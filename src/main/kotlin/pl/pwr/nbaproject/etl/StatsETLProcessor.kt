package pl.pwr.nbaproject.etl

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.stereotype.Service
import pl.pwr.nbaproject.api.StatsClient
import pl.pwr.nbaproject.model.Queue
import pl.pwr.nbaproject.model.amqp.StatsMessage
import pl.pwr.nbaproject.model.api.StatsWrapper
import pl.pwr.nbaproject.model.db.Stats
import reactor.rabbitmq.Receiver
import kotlin.reflect.KClass

@Service
class StatsETLProcessor(
    rabbitReceiver: Receiver,
    objectMapper: ObjectMapper,
    databaseClient: DatabaseClient,
    private val statsClient: StatsClient,
) : AbstractETLProcessor<StatsMessage,
        StatsWrapper,
        List<Stats>>(
    rabbitReceiver,
    objectMapper,
    databaseClient
) {

    override val queue: Queue = Queue.PLAYERS

    override val messageClass: KClass<StatsMessage> = StatsMessage::class

    override suspend fun extract(apiParams: StatsMessage): StatsWrapper = with(apiParams) {
        statsClient.getStats(
            seasons,
            teamIds,
            gameIds,
            postSeason,
            page,
            page
        )
    }

    override suspend fun transform(data: StatsWrapper): List<Stats> = data.data.map { stats ->
        with(stats) {
            Stats(
                id = id,
                playerId = player.id,
                teamId = team.id,
                gameId = game.id,
                homeTeamId = game.homeTeamId,
                homeTeamScore = game.homeTeamScore,
                visitorTeamId = game.visitorTeamId,
                visitorTeamScore = game.visitorTeamScore,
                winnerTeamId = if (game.homeTeamScore > game.visitorTeamScore) game.homeTeamId else game.visitorTeamId,
                season = game.season,
                date = game.date,
                firstName = player.firstName,
                lastName = player.lastName,
                minutes = minutes,
                points = points,
                assists = assists,
                rebounds = rebounds,
                defensiveRebounds = defensiveRebounds,
                offensiveRebounds = offensiveRebounds,
                blocks = blocks,
                steals = steals,
                turnovers = turnovers,
                personalFouls = personalFouls,
                fieldGoalsAttempted = fieldGoalsAttempted,
                fieldGoalsMade = fieldGoalsMade,
                fieldGoalPercentage = fieldGoalPercentage,
                threePointersAttempted = threePointersAttempted,
                threePointersMade = threePointersMade,
                threePointerPercentage = threePointerPercentage,
                freeThrowsAttempted = freeThrowsAttempted,
                freeThrowsMade = freeThrowsMade,
                freeThrowPercentage = freeThrowPercentage,
            )
        }
    }

    override suspend fun load(data: List<Stats>): List<String> = data.map { stats ->
        with(stats) {
            //language=Greenplum
            """
INSERT INTO stats(
    id,
    player_id,
    team_id,
    game_id,
    home_team_id,
    home_team_score,
    visitor_team_id,
    visitor_team_score,
    winner_team_id,
    season,
    date,
    first_name,
    last_name,
    minutes,
    points,
    assists,
    rebounds,
    defensive_rebounds,
    offensive_rebounds,
    blocks,
    steals,
    turnovers,
    personal_fouls,
    field_goals_attempted,
    field_goals_made,
    field_goal_percentage,
    three_pointers_attempted,
    three_pointers_made,
    three_pointer_percentage,
    free_throws_attempted,
    free_throws_made,
    free_throw_percentage
) VALUES (
    $id,
    $playerId,
    $teamId,
    $gameId,
    $homeTeamId,
    $homeTeamScore,
    $visitorTeamId,
    $visitorTeamScore,
    $winnerTeamId,
    $season,
    $date,
    $firstName,
    $lastName,
    $minutes,
    $points,
    $assists,
    $rebounds,
    $defensiveRebounds,
    $offensiveRebounds,
    $blocks,
    $steals,
    $turnovers,
    $personalFouls,
    $fieldGoalsAttempted,
    $fieldGoalsMade,
    $fieldGoalPercentage,
    $threePointersAttempted,
    $threePointersMade,
    $threePointerPercentage,
    $freeThrowsAttempted,
    $freeThrowsMade,
    $freeThrowPercentage
)"""
        }
    }

}
