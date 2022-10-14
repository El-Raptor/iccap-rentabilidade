package br.com.sankhya.dao;

import java.math.BigDecimal;

import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.model.Item;

public class ItemDAO {

	public static Item read(DynamicVO iteVO) throws Exception {
		Item item = new Item();

		item.setCodprod(iteVO.asBigDecimal("CODPROD"));
		item.setCodite(iteVO.asBigDecimal("CODITE"));
		item.setCodlocalorig(new BigDecimal(12004));
		item.setPercdesc(iteVO.asBigDecimal("PERCDESC"));
		item.setQtdneg(iteVO.asBigDecimal("QTD"));
		item.setVlrtot(iteVO.asBigDecimal("VLRTOT"));
		item.setVlracresc(coalesce(iteVO, "VLRACRESC"));
		item.setVlrunit(coalesce(iteVO, "VLRUNIT"));
		item.setVlrdesc(coalesce(iteVO, "VLRDESC"));
		item.computedValues();

		return item;
	}

	private static BigDecimal coalesce(DynamicVO iteVO, String field) {
		return iteVO.asBigDecimal(field) == null ? BigDecimal.ZERO : iteVO.asBigDecimal(field);
	}
}
