package br.com.sankhya.model;

import java.math.BigDecimal;

import br.com.sankhya.dao.ItemDAO;
import br.com.sankhya.jape.vo.DynamicVO;

/**
 * Essa classe representa um item/peça.
 * 
 * @author Felipe S. Lopes (felipe.lopes@sankhya.com.br)
 * @since 2022-09-30
 * @version 1.0.0
 * 
 */
public class Item {
	private BigDecimal codoos;
	private BigDecimal codite;
	private BigDecimal sequencia;
	private BigDecimal codprod;
	private BigDecimal codlocalorig;
	private BigDecimal qtdneg;
	private BigDecimal percdesc;
	private BigDecimal vlrdesc;
	private BigDecimal vlrunit;
	private BigDecimal vlrtot;
	private BigDecimal vlracresc;
	private String usoprod;
	private String codvol;

	public Item() {

	}

	public static Item builder(DynamicVO iteVO, String entityName) throws Exception {
		return ItemDAO.read(iteVO, entityName);
	}

	public void computedValues() {
		setVlrunit((this.vlrunit.multiply(getQtdneg()).add(getVlracresc())).divide(getQtdneg(), 3,
				BigDecimal.ROUND_HALF_UP));
		setVlrdesc(this.vlrdesc.divide(getQtdneg(), 3, BigDecimal.ROUND_HALF_UP));
	}

	public BigDecimal getCodprod() {
		return codprod;
	}

	public void setCodprod(BigDecimal codprod) {
		this.codprod = codprod;
	}

	public String getUsoprod() {
		return usoprod;
	}

	public void setUsoprod(String usoprod) {
		this.usoprod = usoprod;
	}

	public String getCodvol() {
		return codvol;
	}

	public void setCodvol(String codvol) {
		this.codvol = codvol;
	}

	public BigDecimal getCodlocalorig() {
		return codlocalorig;
	}

	public void setCodlocalorig(BigDecimal codlocalorig) {
		this.codlocalorig = codlocalorig;
	}

	public BigDecimal getQtdneg() {
		return qtdneg;
	}

	public void setQtdneg(BigDecimal qtdneg) {
		this.qtdneg = qtdneg;
	}

	public BigDecimal getPercdesc() {
		return percdesc;
	}

	public void setPercdesc(BigDecimal percdesc) {
		this.percdesc = percdesc;
	}

	public BigDecimal getVlrdesc() {
		return vlrdesc;
	}

	public void setVlrdesc(BigDecimal vlrdesc) {
		this.vlrdesc = vlrdesc;
	}

	public BigDecimal getVlrunit() {
		return vlrunit;
	}

	public void setVlrunit(BigDecimal vlrunit) {
		this.vlrunit = vlrunit;
	}

	public BigDecimal getVlrtot() {
		return vlrtot;
	}

	public void setVlrtot(BigDecimal vlrtot) {
		this.vlrtot = vlrtot;
	}

	public BigDecimal getVlracresc() {
		return vlracresc;
	}

	public void setVlracresc(BigDecimal vlracresc) {
		this.vlracresc = vlracresc;
	}

	public BigDecimal getCodite() {
		return codite;
	}

	public void setCodite(BigDecimal codite) {
		this.codite = codite;
	}

	public BigDecimal getCodoos() {
		return codoos;
	}

	public void setCodoos(BigDecimal codoos) {
		this.codoos = codoos;
	}

	public BigDecimal getSequencia() {
		return sequencia;
	}

	public void setSequencia(BigDecimal sequencia) {
		this.sequencia = sequencia;
	}
	
	

}
