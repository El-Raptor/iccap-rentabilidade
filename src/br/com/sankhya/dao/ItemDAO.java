package br.com.sankhya.dao;

import java.math.BigDecimal;

import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.wrapper.JapeFactory;
import br.com.sankhya.jape.wrapper.JapeWrapper;
import br.com.sankhya.model.Item;
import br.com.sankhya.modelcore.util.DynamicEntityNames;

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

	/**
	 * Método que busca e retorna a instância de um registro da tabela de itens
	 * (TGFITE).
	 * 
	 * @param nunota Número único do registro do pedido do item a ser buscado.
	 * @param codite Código da peça do Orçamento/Ordem de Serviço que será buscado
	 *               no item.
	 * @return DynamicVO iteVO instância de um registro da tabela de itens (TGFITE).
	 */
	public static DynamicVO getItemVO(BigDecimal nunota, BigDecimal codite) {
		JapeWrapper iteDAO = JapeFactory.dao(DynamicEntityNames.ITEM_NOTA);
		DynamicVO iteVO = null;
		try {
			iteVO = iteDAO.findOne(" NUNOTA = " + nunota + " AND AD_CODITE = " + codite);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return iteVO;
	}

	private static BigDecimal coalesce(DynamicVO iteVO, String field) {
		return iteVO.asBigDecimal(field) == null ? BigDecimal.ZERO : iteVO.asBigDecimal(field);
	}
}
