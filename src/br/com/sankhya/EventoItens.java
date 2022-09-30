package br.com.sankhya;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;

import br.com.sankhya.dao.ItemDAO;
import br.com.sankhya.extensions.eventoprogramavel.EventoProgramavelJava;
import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.core.JapeSession;
import br.com.sankhya.jape.core.JapeSession.SessionHandle;
import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.event.TransactionContext;
import br.com.sankhya.jape.util.JapeSessionContext;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.vo.PrePersistEntityState;
import br.com.sankhya.jape.wrapper.JapeFactory;
import br.com.sankhya.jape.wrapper.JapeWrapper;
import br.com.sankhya.model.Item;
import br.com.sankhya.modelcore.auth.AuthenticationInfo;
import br.com.sankhya.modelcore.comercial.centrais.CACHelper;
import br.com.sankhya.modelcore.util.DynamicEntityNames;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;

/**
 * Este programa realiza o espelhamento dos valores dos itens de um ordem de
 * serviço da tela de Ordens de Serviços para os itens da nota.
 * 
 * @author Felipe S. Lopes (felipe.lopes@sankhya.com.br)
 * @since 2022-09-30
 * @version 0.1.0
 */
public class EventoItens implements EventoProgramavelJava {

	@Override
	public void afterInsert(PersistenceEvent ctx) throws Exception {
		// TODO Se a OS for do tipo Orçamento, sempre que uma peça for adicionada essa
		// peça será refletida como um item na nota do orçamento.

		SessionHandle hnd = null;
		
		//JdbcWrapper jdbc = null;

		// try {
		hnd = JapeSession.open(); 
		//EntityFacade dwfEntityFacade = EntityFacadeFactory.getDWFFacade();
		//jdbc = dwfEntityFacade.getJdbcWrapper();

		//Nota nota = new Nota();
		DynamicVO iteVO = (DynamicVO) ctx.getVo();

		BigDecimal codoos = iteVO.asBigDecimal("CODOOS");
		
		JapeWrapper oscabDAO = JapeFactory.dao("AD_OOSCAB");
		DynamicVO oscabVO = oscabDAO.findByPK(codoos);
		
		JapeWrapper cabDAO = JapeFactory.dao(DynamicEntityNames.CABECALHO_NOTA);
		DynamicVO cabVO = cabDAO.findOne(" AD_CODOS = " + codoos);

		if (oscabVO.asString("TIPOLANCAMENTO").equals("O")) {

			/* Inicializando item */
			Item item = ItemDAO.read(iteVO);

			adicionaItemPedido(cabVO, item);
		}
		/*
		 * } catch (Exception e) { e.printStackTrace(); e.getMessage();
		 * //MGEModelException.throwMe(e); } finally { JapeSession.close(hnd); }
		 */
		JapeSession.close(hnd);

	}

	@Override
	public void afterUpdate(PersistenceEvent paramPersistenceEvent) throws Exception {
		// TODO Auto-generated method stub

	}

	private void adicionaItemPedido(DynamicVO nota, Item item) throws Exception {
		Collection<PrePersistEntityState> itensNota = new ArrayList<PrePersistEntityState>();
		AuthenticationInfo authInfo = AuthenticationInfo.getCurrent();
		
		// Variáveis do sistema nos quais permite recalcular o financeiro
		JapeSessionContext.putProperty("br.com.sankhya.com.CentralCompraVenda", Boolean.TRUE);
		JapeSessionContext.putProperty("ItemNota.incluindo.alterando.pela.central", Boolean.TRUE);

		EntityFacade dwfFacade = EntityFacadeFactory.getDWFFacade();
		DynamicVO itemVO = (DynamicVO) dwfFacade.getDefaultValueObjectInstance(DynamicEntityNames.ITEM_NOTA);

		itemVO.setPrimaryKey(null);
		itemVO.setProperty("NUNOTA", nota.asBigDecimal("NUNOTA"));
		itemVO.setProperty("CODEMP", nota.asBigDecimal("CODEMP"));
		itemVO.setProperty("CODVEND", nota.asBigDecimal("CODVEND"));
		itemVO.setProperty("CODPROD", item.getCodprod());
		itemVO.setProperty("USOPROD", item.getUsoprod());
		itemVO.setProperty("CODVOL", item.getCodvol());
		itemVO.setProperty("CODLOCALORIG", item.getCodlocalorig());
		itemVO.setProperty("QTDNEG", item.getQtdneg());
		itemVO.setProperty("PERCDESC",item.getPercdesc());
		itemVO.setProperty("VLRDESC", item.getVlrdesc());
		itemVO.setProperty("VLRUNIT", item.getVlrunit());
		itemVO.setProperty("VLRTOT", item.getVlrtot());

		PrePersistEntityState itemMontado = PrePersistEntityState.build(dwfFacade, DynamicEntityNames.ITEM_NOTA,
				itemVO);

		itensNota.add(itemMontado);

		CACHelper sistema = new CACHelper();

		sistema.incluirAlterarItem(nota.asBigDecimal("NUNOTA"), authInfo, itensNota, true);
	}

	@Override
	public void afterDelete(PersistenceEvent paramPersistenceEvent) throws Exception {
	}

	@Override
	public void beforeCommit(TransactionContext paramTransactionContext) throws Exception {
	}

	@Override
	public void beforeDelete(PersistenceEvent paramPersistenceEvent) throws Exception {
	}

	@Override
	public void beforeInsert(PersistenceEvent paramPersistenceEvent) throws Exception {
	}

	@Override
	public void beforeUpdate(PersistenceEvent paramPersistenceEvent) throws Exception {
	}

}
