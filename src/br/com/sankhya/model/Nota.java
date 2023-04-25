package br.com.sankhya.model;

import java.math.BigDecimal;
import java.sql.Timestamp;

import br.com.sankhya.dao.NotaDAO;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.vo.DynamicVO;

/**
 * Essa classe representa uma nota/orçamento/ordem de serviço.
 * 
 * @author Felipe S. Lopes (felipe.lopes@sankhya.com.br)
 * @since 2022-09-28
 * @version 1.0.0
 * 
 */
public class Nota {
	private BigDecimal nunota;
	private BigDecimal codtipoper;
	private Timestamp dhtipoper;
	private BigDecimal tipmov;
	private BigDecimal codparc;
	private BigDecimal codemp;
	private BigDecimal codtipvenda;
	private Timestamp dhtipvenda;
	private BigDecimal codusu;
	private BigDecimal codvend;
	private BigDecimal codos;
	private BigDecimal vlrnota;
	private BigDecimal desctot;
	private BigDecimal codmotivoabert;
	private String tipolancamento;
	private String observacao;

	public Nota() {

	}

	public static Nota builder(DynamicVO cabosVO, JdbcWrapper jdbc) throws Exception {
		return NotaDAO.read(cabosVO, jdbc);
	}

	public void setTipolancamento(String tipolancamento) {
		this.tipolancamento = tipolancamento;
	}

	public String getTipolancamento() {
		return tipolancamento;
	}

	public BigDecimal getCodparc() {
		return codparc;
	}

	public void setCodparc(BigDecimal codparc) {
		this.codparc = codparc;
	}

	public BigDecimal getCodemp() {
		return codemp;
	}

	public void setCodemp(BigDecimal codemp) {
		this.codemp = codemp;
	}

	public BigDecimal getCodtipvenda() {
		return codtipvenda;
	}

	public void setCodtipvenda(BigDecimal codtipvenda) {
		this.codtipvenda = codtipvenda;
	}

	public BigDecimal getCodusu() {
		return codusu;
	}

	public void setCodusu(BigDecimal codusu) {
		this.codusu = codusu;
	}

	public BigDecimal getCodvend() {
		return codvend;
	}

	public void setCodvend(BigDecimal codvend) {
		this.codvend = codvend;
	}

	public BigDecimal getCodos() {
		return codos;
	}

	public void setCodos(BigDecimal codos) {
		this.codos = codos;
	}

	public BigDecimal getVlrnota() {
		return vlrnota;
	}

	public void setVlrnota(BigDecimal vlrnota) {
		this.vlrnota = vlrnota;
	}

	public BigDecimal getDesctot() {
		return desctot;
	}

	public void setDesctot(BigDecimal desctot) {
		this.desctot = desctot;
	}

	public String getObservacao() {
		return observacao;
	}

	public void setObservacao(String observacao) {
		this.observacao = observacao;
	}

	public BigDecimal getNunota() {
		return nunota;
	}

	public void setNunota(BigDecimal nunota) {
		this.nunota = nunota;
	}

	public BigDecimal getCodtipoper() {
		return codtipoper;
	}

	public void setCodtipoper(BigDecimal codtipoper) {
		this.codtipoper = codtipoper;
	}

	public BigDecimal getTipmov() {
		return tipmov;
	}

	public void setTipmov(BigDecimal tipmov) {
		this.tipmov = tipmov;
	}

	public BigDecimal getCodmotivoabert() {
		return codmotivoabert;
	}

	public void setCodmotivoabert(BigDecimal codmotivoabert) {
		this.codmotivoabert = codmotivoabert;
	}

	public Timestamp getDhtipoper() {
		return dhtipoper;
	}

	public void setDhtipoper(Timestamp dhtipoper) {
		this.dhtipoper = dhtipoper;
	}

	public Timestamp getDhtipvenda() {
		return dhtipvenda;
	}

	public void setDhtipvenda(Timestamp dhtipvenda) {
		this.dhtipvenda = dhtipvenda;
	}

}
