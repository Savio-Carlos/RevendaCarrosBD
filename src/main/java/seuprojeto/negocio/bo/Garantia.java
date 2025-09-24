// Pacote: seuprojeto.classe
package seuprojeto.negocio.bo;

import java.util.Date;

public class Garantia {

    private int idGarantia;
    private Date dataInicio;
    private int kmLimiteRevisao;
    private String dataExpiracaoGarantia; // Manter como String conforme MER, mas Date seria ideal
    private int idStatusGarantia;

    public Garantia() {
    }

    public int getIdGarantia() {
        return idGarantia;
    }

    public void setIdGarantia(int idGarantia) {
        this.idGarantia = idGarantia;
    }

    public Date getDataInicio() {
        return dataInicio;
    }

    public void setDataInicio(Date dataInicio) {
        this.dataInicio = dataInicio;
    }

    public int getKmLimiteRevisao() {
        return kmLimiteRevisao;
    }

    public void setKmLimiteRevisao(int kmLimiteRevisao) {
        this.kmLimiteRevisao = kmLimiteRevisao;
    }

    public String getDataExpiracaoGarantia() {
        return dataExpiracaoGarantia;
    }

    public void setDataExpiracaoGarantia(String dataExpiracaoGarantia) {
        this.dataExpiracaoGarantia = dataExpiracaoGarantia;
    }

    public int getIdStatusGarantia() {
        return idStatusGarantia;
    }

    public void setIdStatusGarantia(int idStatusGarantia) {
        this.idStatusGarantia = idStatusGarantia;
    }

    public void setStatus(StatusGarantia status) {

    }
}
