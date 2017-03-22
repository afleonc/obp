package models;

import java.util.Set;

/**
 * Created by afleo on 21/03/2017.
 */
public class SolicitudHistoriaObp {
    private String tipoId;
    private String numeroId;
    private Boolean solicitudObtenida;
    private Set<Integer> mensajesOBP;
    private Set<String> estadosEmisor;

    public String getTipoId() {
        return tipoId;
    }

    public void setTipoId(String tipoId) {
        this.tipoId = tipoId;
    }

    public String getNumeroId() {
        return numeroId;
    }

    public void setNumeroId(String numeroId) {
        this.numeroId = numeroId;
    }

    public Set<Integer> getMensajesOBP() {
        return mensajesOBP;
    }

    public void setMensajesOBP(Set<Integer> mensajesOBP) {
        this.mensajesOBP = mensajesOBP;
    }

    public Set<String> getEstadosEmisor() {
        return estadosEmisor;
    }

    public void setEstadosEmisor(Set<String> estadosEmisor) {
        this.estadosEmisor = estadosEmisor;
    }

    public Boolean getSolicitudObtenida() {
        return solicitudObtenida;
    }

    public void setSolicitudObtenida(Boolean solicitudObtenida) {
        this.solicitudObtenida = solicitudObtenida;
    }
}
