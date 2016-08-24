package co.touchlab.droidconandroid.data

import java.util.*

class SponsorsResponse {

    var totalSpanCount: Int = 0
    var sponsors: List<Sponsor> = Collections.emptyList()

    class Sponsor(val spanCount: Int, val sponsorName: String, val sponsorImage: String, val sponsorLink: String)

}
