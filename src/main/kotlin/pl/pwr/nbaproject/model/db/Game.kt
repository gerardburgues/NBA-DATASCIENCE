package pl.pwr.nbaproject.model.db

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDate

@Table(GAMES_TABLE)
data class Game(
    @Id var id: Long,
    var date: LocalDate,
    var homeTeamScore: Int,
    var visitorTeamScore: Int,
    var season: Int,
    var period: Int,
    var status: String,
    var time: String,
    var postseason: Boolean,
    var homeTeamId: Long,
    var visitorTeamId: Long,
    var winnerTeamId: Long,

    var homeTeam: Team? = null,
    var visitorTeam: Team? = null,
    var winnerTeam: Team? = null,
)
