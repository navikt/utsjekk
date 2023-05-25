package no.nav.dagpenger.iverksett.infrastruktur.util

import no.nav.dagpenger.iverksett.kontrakter.iverksett.Vilkårsvurdering
import no.nav.dagpenger.iverksett.kontrakter.felles.RegelId
import no.nav.dagpenger.iverksett.kontrakter.felles.SvarId
import no.nav.dagpenger.iverksett.kontrakter.felles.VilkårType
import no.nav.dagpenger.iverksett.kontrakter.felles.Vilkårsresultat

object VilkårsvurderingUtil {

    fun hentHarSagtOppEllerRedusertFraVurderinger(vilkårsvurderinger: List<Vilkårsvurdering>): Boolean? {
        val vilkårsvurdering = vilkårsvurderinger.find { it.vilkårType == VilkårType.SAGT_OPP_ELLER_REDUSERT }
            ?: error("Finner ikke vurderingen for sagt opp eller redusert")

        return if (vilkårsvurdering.resultat == Vilkårsresultat.SKAL_IKKE_VURDERES) {
            null
        } else {
            val vurdering = vilkårsvurdering.delvilkårsvurderinger.flatMap { it.vurderinger }
                .firstOrNull { it.regelId == RegelId.SAGT_OPP_ELLER_REDUSERT }
                ?: error("Finner ikke delvilkårsvurderingen for sagt opp eller redusert stilling")
            harSagtOppEllerRedusertStilling(vurdering.svar)
        }
    }

    private fun harSagtOppEllerRedusertStilling(svarId: SvarId?) = when (svarId) {
        SvarId.JA -> true
        SvarId.NEI -> false
        else -> null
    }
}
