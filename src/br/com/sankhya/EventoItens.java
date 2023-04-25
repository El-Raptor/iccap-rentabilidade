package br.com.sankhya;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import br.com.sankhya.dao.ItemDAO;
import br.com.sankhya.dao.NotaDAO;
import br.com.sankhya.extensions.eventoprogramavel.EventoProgramavelJava;
import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.bmp.PersistentLocalEntity;
import br.com.sankhya.jape.core.JapeSession;
import br.com.sankhya.jape.core.JapeSession.SessionHandle;
import br.com.sankhya.jape.dao.EntityDAO;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.event.TransactionContext;
import br.com.sankhya.jape.util.JapeSessionContext;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.vo.PrePersistEntityState;
import br.com.sankhya.model.Item;
import br.com.sankhya.modelcore.MGEModelException;
import br.com.sankhya.modelcore.auth.AuthenticationInfo;
import br.com.sankhya.modelcore.comercial.CentralItemNota;
import br.com.sankhya.modelcore.comercial.centrais.CACHelper;
import br.com.sankhya.modelcore.util.DynamicEntityNames;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;
import br.com.sankhya.modelcore.util.ListenerParameters;
import br.com.sankhya.ws.ServiceContext;

/**
 * Este programa realiza o espelhamento dos valores dos itens de um ordem de
 * serviço da tela de Ordens de Serviços para os itens da nota.
 * 
 * @author Felipe S. Lopes (felipe.lopes@sankhya.com.br)
 * @since 2022-09-30
 * @version 1.1.0
 */
public class EventoItens implements EventoProgramavelJava {

	@Override
	public void afterInsert(PersistenceEvent ctx) throws Exception {

		SessionHandle hnd = null;
		JdbcWrapper jdbc = null;

		try {
			hnd = JapeSession.open();
			EntityFacade dwfEntityFacade = EntityFacadeFactory.getDWFFacade();
			jdbc = dwfEntityFacade.getJdbcWrapper();
			// Nota nota = new Nota();
			DynamicVO pecaVO = (DynamicVO) ctx.getVo();

			BigDecimal codoos = pecaVO.asBigDecimal("CODOOS");

			DynamicVO oscabVO = NotaDAO.getCabOSVO(codoos);
			DynamicVO cabVO = NotaDAO.getCabVO(oscabVO.asBigDecimal("NUNOTA"));

			if (oscabVO.asString("TIPOLANCAMENTO").equals("O")) {

				/* Inicializando item */
				String entityName = ctx.getTargetDAO().getEntityName();
				Item item = Item.builder(pecaVO, entityName);

				DynamicVO itemVO = addItemOrder(cabVO, item);
				item.setSequencia(itemVO.asBigDecimal("SEQUENCIA"));
				

				/* Vincula a sequência criada ao orçamento da OS. */
				ItemDAO.setSequencia(jdbc, item, entityName);
				ItemDAO.updateProfit(jdbc, pecaVO, entityName);
				
		
			}

		} catch (Exception e) {
			e.printStackTrace();
			e.getMessage();
			MGEModelException.throwMe(e);
		} finally {
			JapeSession.close(hnd);
		}
	}

	@Override
	public void beforeUpdate(PersistenceEvent ctx) throws Exception {
		SessionHandle hnd = null;
		JdbcWrapper jdbc = null;

		try {
			hnd = JapeSession.open();
			EntityFacade dwfEntityFacade = EntityFacadeFactory.getDWFFacade();
			jdbc = dwfEntityFacade.getJdbcWrapper();
			DynamicVO pecaVO = (DynamicVO) ctx.getVo();

			/* Incializando o item */
			String entityName = ctx.getTargetDAO().getEntityName();
			Item item = Item.builder(pecaVO, entityName);

			DynamicVO oscabVO = NotaDAO.getCabOSVO(pecaVO.asBigDecimal("CODOOS"));
			DynamicVO cabVO = NotaDAO.getCabVO(oscabVO.asBigDecimal("NUNOTA"));
			DynamicVO iteVO = ItemDAO.getItemVO(cabVO.asBigDecimal("NUNOTA"), pecaVO.asBigDecimal("SEQUENCIA"));

			updateItemOrder(item, iteVO, cabVO);
			
			ItemDAO.updateProfit(jdbc, pecaVO, entityName);

		} catch (Exception e) {
			e.printStackTrace();
			e.getMessage();
			MGEModelException.throwMe(e);
		} finally {
			JapeSession.close(hnd);
		}

	}

	@Override
	public void beforeDelete(PersistenceEvent ctx) throws Exception {
		JapeSession.SessionHandle hnd = null;

		try {
			DynamicVO pecaVO = (DynamicVO) ctx.getVo();
			hnd = JapeSession.open();

			DynamicVO oscabVO = NotaDAO.getCabOSVO(pecaVO.asBigDecimal("CODOOS"));
			DynamicVO cabVO = NotaDAO.getCabVO(oscabVO.asBigDecimal("NUNOTA"));
			DynamicVO itemVO = ItemDAO.getItemVO(cabVO.asBigDecimal("NUNOTA"), pecaVO.asBigDecimal("SEQUENCIA"));

			deleteItem(itemVO);

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			JapeSession.close(hnd);
		}
	}

	/**
	 * Este método adiciona a peça na tabela de itens da nota do orçamento.
	 * 
	 * @param nota A instância do registro da tabela de notas (TGFCAB).
	 * @param item A instância do registro da tabela de itens (TGFITE).
	 * @throws Exception
	 */
	private DynamicVO addItemOrder(DynamicVO nota, Item item) throws Exception {
		Collection<PrePersistEntityState> itensNota = new ArrayList<PrePersistEntityState>();
		AuthenticationInfo authInfo = AuthenticationInfo.getCurrent();

		// Variáveis do sistema nos quais permitem recalcular o financeiro
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
		itemVO.setProperty("PERCDESC", item.getPercdesc());
		itemVO.setProperty("VLRDESC", item.getVlrdesc());
		itemVO.setProperty("VLRUNIT", item.getVlrunit());
		itemVO.setProperty("VLRTOT", item.getVlrtot());

		PrePersistEntityState itemMontado = PrePersistEntityState.build(dwfFacade, DynamicEntityNames.ITEM_NOTA,
				itemVO);

		itensNota.add(itemMontado);

		CACHelper sistema = new CACHelper();

		sistema.incluirAlterarItem(nota.asBigDecimal("NUNOTA"), authInfo, itensNota, true);

		return updateItemOrder(item, itemVO, nota);

	}

	/**
	 * Este método atualiza o item da nota passado.
	 * 
	 * @param item   instância de um item com as propriedades que serão usadas para
	 *               alterar o item no sistema.
	 * @param itemVO instância de um registro do item de uma nota que será alterado.
	 * @param cabVO  instância de um registro da nota que contém o item que será
	 *               alterado.
	 * @throws Exception
	 */
	private static DynamicVO updateItemOrder(Item item, DynamicVO itemVO, DynamicVO cabVO) throws Exception {
		// Variáveis do sistema nos quais permitem recalcular o financeiro
		JapeSessionContext.putProperty("br.com.sankhya.com.CentralCompraVenda", Boolean.TRUE);
		JapeSessionContext.putProperty("ItemNota.incluindo.alterando.pela.central", Boolean.TRUE);

		ServiceContext service = ServiceContext.getCurrent();

		BigDecimal vlrunit = item.getVlrunit();

		itemVO.setProperty("CODPROD", item.getCodprod());
		itemVO.setProperty("QTDNEG", item.getQtdneg());
		itemVO.setProperty("VLRDESC", item.getVlrdesc());
		itemVO.setProperty("PERCDESC", item.getPercdesc());
		itemVO.setProperty("VLRUNIT", vlrunit);
		itemVO.setProperty("VLRTOT", vlrunit.multiply(item.getQtdneg()));

		CentralItemNota itemNota = new CentralItemNota();
		itemNota.recalcularValores("VLRUNIT", vlrunit.toString(), itemVO, cabVO.asBigDecimal("NUNOTA"));

		List<DynamicVO> itensFatura = new ArrayList<DynamicVO>();
		itensFatura.add(itemVO);

		CACHelper cacHelper = new CACHelper();
		cacHelper.incluirAlterarItem(cabVO.asBigDecimal("NUNOTA"), service, null, false, itensFatura);
		
		return itemVO;
	}

	/**
	 * Este método faz a deleção de um item da nota passado. A deleção é feita
	 * através do método <code>excluirItemNota</code> da classe
	 * <code>CACHelper</code>, passando a instância do registro do item, além de
	 * outras variáveis que são buscadas nesse método.
	 * 
	 * @param itemVO instância do registro do item de uma nota.
	 * @throws Exception
	 */
	private static void deleteItem(DynamicVO itemVO) throws Exception {
		// Variáveis do sistema nos quais permitem recalcular o financeiro
		JapeSessionContext.putProperty("br.com.sankhya.com.CentralCompraVenda", Boolean.TRUE);
		JapeSessionContext.putProperty("ItemNota.incluindo.alterando.pela.central", Boolean.TRUE);

		EntityFacade dwfFacade = EntityFacadeFactory.getDWFFacade();
		ServiceContext service = ServiceContext.getCurrent();
		EntityDAO dao = dwfFacade.getDAOInstance(DynamicEntityNames.ITEM_NOTA);
		Object[] keys = { itemVO.asBigDecimal("NUNOTA"), itemVO.asBigDecimal("SEQUENCIA") };
		PersistentLocalEntity entity = dwfFacade.findEntityByPrimaryKey(dao.getEntityName(), keys);

		CACHelper cacHelper = new CACHelper();
		JapeSessionContext.putProperty(ListenerParameters.CENTRAIS, Boolean.TRUE);
		cacHelper.excluirItemNota(itemVO, dwfFacade, dao, entity, service);
	}

	@Override
	public void afterDelete(PersistenceEvent paramPersistenceEvent) throws Exception {
	}

	@Override
	public void beforeCommit(TransactionContext paramTransactionContext) throws Exception {
	}

	@Override
	public void beforeInsert(PersistenceEvent paramPersistenceEvent) throws Exception {
	}

	@Override
	public void afterUpdate(PersistenceEvent paramPersistenceEvent) throws Exception {
	}

}
