package br.com.sankhya.model;

import java.math.BigDecimal;

import br.com.sankhya.dao.NotaDAO;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.event.PersistenceEvent;

public class Nota {
	private BigDecimal nunota;
	private BigDecimal codtipoper;
	private BigDecimal tipmov;
	private BigDecimal codparc;
	private BigDecimal codemp;
	private BigDecimal codtipvenda;
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

	public void buildNota(PersistenceEvent ctx, JdbcWrapper jdbc) throws Exception {
		Nota nota = NotaDAO.read(ctx, jdbc);

		setCodemp(nota.getCodemp());
		setCodmotivoabert(nota.getCodmotivoabert());
		setCodos(nota.getCodos());
		setCodparc(nota.getCodparc());
		setCodtipoper(nota.getCodtipoper());
		setCodtipvenda(nota.getCodtipvenda());
		setCodusu(nota.getCodusu());
		setCodvend(nota.getCodvend());
		setDesctot(nota.getDesctot());
		setObservacao(nota.getObservacao());
		setTipolancamento(nota.getTipolancamento());
		setVlrnota(nota.getVlrnota());

		setNunota(NotaDAO.readNunota(jdbc, getCodos()));
	}

	public void buildNewNota(PersistenceEvent ctx, JdbcWrapper jdbc) throws Exception {
		Nota nota = NotaDAO.read(ctx, jdbc);

		setCodemp(nota.getCodemp());
		setCodmotivoabert(nota.getCodmotivoabert());
		setCodos(nota.getCodos());
		setCodparc(nota.getCodparc());
		setCodtipoper(nota.getCodtipoper());
		setCodtipvenda(nota.getCodtipvenda());
		setCodusu(nota.getCodusu());
		setCodvend(nota.getCodvend());
		setDesctot(nota.getDesctot());
		setObservacao(nota.getObservacao());
		setTipolancamento(nota.getTipolancamento());
		setVlrnota(nota.getVlrnota());

		// TODO: CODOS não pode ser null
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


}
