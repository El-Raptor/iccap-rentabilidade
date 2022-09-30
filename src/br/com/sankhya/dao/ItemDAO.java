package br.com.sankhya.dao;

import java.math.BigDecimal;

import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.model.Item;

public class ItemDAO {

	public static Item read(DynamicVO iteVO) {
		Item item = new Item();
		
		item.setCodprod(iteVO.asBigDecimal("CODPROD"));
		item.setCodlocalorig(new BigDecimal(12004));
		item.setPercdesc(iteVO.asBigDecimal("PERCDESC"));
		item.setVlrdesc(iteVO.asBigDecimal("VLRDESC"));
		item.setQtdneg(iteVO.asBigDecimal("QTD"));
		item.setVlrunit(iteVO.asBigDecimal("VLRUNIT"));
		item.setVlrtot(iteVO.asBigDecimal("VLRTOT"));
		
		return item;
	}
}
