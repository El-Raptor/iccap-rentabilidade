package br.com.sankhya.model;

import java.math.BigDecimal;

public class Item {
	private BigDecimal codprod;
	private BigDecimal codlocalorig;
	private BigDecimal qtdneg;
	private BigDecimal percdesc;
	private BigDecimal vlrdesc;
	private BigDecimal vlrunit;
	private BigDecimal vlrtot;
	private String usoprod;
	private String codvol;
	
	public Item () {
		
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
		
}
