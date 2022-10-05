package br.com.sankhya;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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
import br.com.sankhya.modelcore.comercial.CentralItemNota;
import br.com.sankhya.modelcore.comercial.centrais.CACHelper;
import br.com.sankhya.modelcore.util.DynamicEntityNames;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;
import br.com.sankhya.ws.ServiceContext;

/**
 * Este programa realiza o espelhamento dos valores dos itens de um ordem de
 * servi�o da tela de Ordens de Servi�os para os itens da nota.
 * 
 * @author Felipe S. Lopes (felipe.lopes@sankhya.com.br)
 * @since 2022-09-30
 * @version 0.1.0
 */
public class EventoItens implements EventoProgramavelJava {

	@Override
	public void afterInsert(PersistenceEvent ctx) throws Exception {
		// TODO Se a OS for do tipo Or�amento, sempre que uma pe�a for adicionada essa
		// pe�a ser� refletida como um item na nota do or�amento.

		SessionHandle hnd = null;

		// JdbcWrapper jdbc = null;

		// try {
		hnd = JapeSession.open();
		// EntityFacade dwfEntityFacade = EntityFacadeFactory.getDWFFacade();
		// jdbc = dwfEntityFacade.getJdbcWrapper();

		// Nota nota = new Nota();
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
	public void afterUpdate(PersistenceEvent ctx) throws Exception {
		SessionHandle hnd = null;

		// JdbcWrapper jdbc = null;

		// try {
		hnd = JapeSession.open();
		DynamicVO itemVO = (DynamicVO) ctx.getVo();
		
		Item item = new Item();
		item = ItemDAO.read(itemVO);
		
		// TODO: Fazer um m�todo para buscar o VO da CAB.
		BigDecimal codoos = itemVO.asBigDecimal("CODOOS");
		
		JapeWrapper cabDAO = JapeFactory.dao(DynamicEntityNames.CABECALHO_NOTA);
		DynamicVO cabVO = cabDAO.findOne(" AD_CODOS = " + codoos);
		
		JapeWrapper iteDAO = JapeFactory.dao(DynamicEntityNames.ITEM_NOTA);
		DynamicVO iteVO = iteDAO.findByPK(cabVO.asBigDecimal("NUNOTA"), itemVO.asBigDecimal("CODITE"));
		
		atualizarItemNota(item, iteVO, cabVO);
		
		
		/*
		 * } catch (Exception e) { e.printStackTrace(); e.getMessage();
		 * //MGEModelException.throwMe(e); } finally { JapeSession.close(hnd); }
		 */
		JapeSession.close(hnd);

	}

	private void adicionaItemPedido(DynamicVO nota, Item item) throws Exception {
		Collection<PrePersistEntityState> itensNota = new ArrayList<PrePersistEntityState>();
		AuthenticationInfo authInfo = AuthenticationInfo.getCurrent();

		// Vari�veis do sistema nos quais permitem recalcular o financeiro
		JapeSessionContext.putProperty("br.com.sankhya.com.CentralCompraVenda", Boolean.TRUE);
		JapeSessionContext.putProperty("ItemNota.incluindo.alterando.pela.central", Boolean.TRUE);

		EntityFacade dwfFacade = EntityFacadeFactory.getDWFFacade();
		DynamicVO itemVO = (DynamicVO) dwfFacade.getDefaultValueObjectInstance(DynamicEntityNames.ITEM_NOTA);

		/*if (item != null)
			throw new Exception("Vlr Unit; " + item.getVlrunit() + " Vlr. Desc: " + item.getVlrdesc());*/
		
		itemVO.setPrimaryKey(null);
		itemVO.setProperty("NUNOTA", nota.asBigDecimal("NUNOTA"));
		itemVO.setProperty("CODEMP", nota.asBigDecimal("CODEMP"));
		itemVO.setProperty("CODVEND", nota.asBigDecimal("CODVEND"));
		itemVO.setProperty("CODPROD", item.getCodprod());
		itemVO.setProperty("USOPROD", item.getUsoprod());
		itemVO.setProperty("CODVOL", item.getCodvol());
		itemVO.setProperty("CODLOCALORIG", item.getCodlocalorig());
		itemVO.setProperty("QTDNEG", item.getQtdneg());
		itemVO.setProperty("PERCDESC", item.getPercdesc());
		itemVO.setProperty("VLRDESC", item.getVlrdesc());
		itemVO.setProperty("VLRUNIT", item.getVlrunit());
		itemVO.setProperty("VLRTOT", item.getVlrtot());
		

		PrePersistEntityState itemMontado = PrePersistEntityState.build(dwfFacade, DynamicEntityNames.ITEM_NOTA,
				itemVO);

		itensNota.add(itemMontado);

		CACHelper sistema = new CACHelper();

		sistema.incluirAlterarItem(nota.asBigDecimal("NUNOTA"), authInfo, itensNota, true);
		
		atualizarItemNota(item, itemVO, nota);
	}

	private static void atualizarItemNota(Item item, DynamicVO itemVO, DynamicVO cabVO) throws Exception {
		// Vari�veis do sistema nos quais permitem recalcular o financeiro
		JapeSessionContext.putProperty("br.com.sankhya.com.CentralCompraVenda", Boolean.TRUE);
		JapeSessionContext.putProperty("ItemNota.incluindo.alterando.pela.central", Boolean.TRUE);

		ServiceContext service = ServiceContext.getCurrent();

		BigDecimal vlrunit = item.getVlrunit();

		itemVO.setProperty("CODPROD", item.getCodprod());
		itemVO.setProperty("QTDNEG", item.getQtdneg());
		itemVO.setProperty("VLRDESC", item.getVlrdesc());
		itemVO.setProperty("VLRUNIT", vlrunit);
		itemVO.setProperty("VLRTOT", vlrunit.multiply(item.getQtdneg()));

		CentralItemNota itemNota = new CentralItemNota();
		itemNota.recalcularValores("VLRUNIT", vlrunit.toString(), itemVO, cabVO.asBigDecimal("NUNOTA"));

		List<DynamicVO> itensFatura = new ArrayList<DynamicVO>();
		itensFatura.add(itemVO);

		CACHelper cacHelper = new CACHelper();
		cacHelper.incluirAlterarItem(cabVO.asBigDecimal("NUNOTA"), service, null, false, itensFatura);
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