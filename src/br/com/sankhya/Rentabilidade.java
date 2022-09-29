package br.com.sankhya;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.util.Iterator;

import com.sankhya.util.TimeUtils;

import br.com.sankhya.dao.NotaDAO;
import br.com.sankhya.extensions.eventoprogramavel.EventoProgramavelJava;
import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.core.JapeSession;
import br.com.sankhya.jape.core.JapeSession.SessionHandle;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.event.TransactionContext;
import br.com.sankhya.jape.sql.NativeSql;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.vo.VOProperty;
import br.com.sankhya.jape.wrapper.JapeFactory;
import br.com.sankhya.jape.wrapper.JapeWrapper;
import br.com.sankhya.jape.wrapper.fluid.FluidCreateVO;
import br.com.sankhya.model.Nota;
import br.com.sankhya.modelcore.util.DynamicEntityNames;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;

/**
 * Este programa realiza o espelhamento dos valores do cabe�alho da tela de
 * Ordens de Servi�os para o cabe�alho da nota.
 * 
 * @author Felipe S. Lopes (felipe.lopes@sankhya.com.br)
 * @since 2022-09-22
 * @version 0.1.0
 * 
 */
public class Rentabilidade implements EventoProgramavelJava {

	@Override
	public void afterInsert(PersistenceEvent ctx) throws Exception {
		// TODO Quando um or�amento da tela de Ordens de Servi�o for criado
		// criar uma nota com os valores do cabe�alho deste or�amento.

		SessionHandle hnd = null;
		JdbcWrapper jdbc = null;

		//try {
		hnd = JapeSession.open();
		EntityFacade dwfEntityFacade = EntityFacadeFactory.getDWFFacade();
		jdbc = dwfEntityFacade.getJdbcWrapper();

		Nota nota = NotaDAO.read(ctx, jdbc);
		
		if (nota.getTipolancamento().equals("O")) {
					
				BigDecimal nunotaTemplate = null;
				NativeSql sql = new NativeSql(jdbc);
				sql.appendSql("SELECT NUNOTA ");
				sql.appendSql("FROM TGFCAB ");
				sql.appendSql("WHERE CODTIPOPER = :CODTIPOPER AND ROWNUM = 1 ");
				sql.appendSql("ORDER BY DTNEG DESC");
				sql.setNamedParameter("CODTIPOPER", nota.getCodtipoper());
				
				ResultSet result = sql.executeQuery();
				
				if (result.next())
					nunotaTemplate = result.getBigDecimal("NUNOTA");

				result.close();
				
				/*if (tipolancamento.equals("O"))
					throw new Exception("Teste: " + nunotaTemplate);
*/
				createNovaNota(nota, nunotaTemplate);

			/*} catch (Exception e) {
				e.printStackTrace();
				e.getMessage();
				//MGEModelException.throwMe(e);
			} finally {
				JapeSession.close(hnd);
			}*/
				JapeSession.close(hnd);

		}

		// TODO Se for um servi�o j� faturado, vai verificar qual o c�dgigo de OS
		// de origem, buscar o nro. �nico da nota dessa OS e fazer essa nota ser
		// faturada.

	}

	@Override
	public void afterUpdate(PersistenceEvent arg0) throws Exception {
		// TODO Quando um or�amento ou um servi�o for alterado

	}

	public static DynamicVO createNovaNota(Nota orcamento, BigDecimal nunotaTemplate) throws Exception {

		JapeWrapper cabDAO = JapeFactory.dao(DynamicEntityNames.CABECALHO_NOTA);

		final JapeWrapper tipoOperacaoDAO = JapeFactory.dao(DynamicEntityNames.TIPO_OPERACAO);

		if (nunotaTemplate == null)
			throw new Exception("Nota modelo de opera��o de pedido de venda n�o existe ou n�o "
					+ "foi cadastrada corretamentre na tela de preferencias panorama");

		final DynamicVO cabTemplate = cabDAO.findByPK(nunotaTemplate);

		if (cabTemplate == null)
			throw new Exception("Nota modelo de opera��o de pedido de venda n�o existe ou n�o foi "
					+ "cadastrada corretamentre na tela de preferencias panorama");

		final DynamicVO topDoModelo = tipoOperacaoDAO.findByPK(cabTemplate.asBigDecimal("CODTIPOPER"),
				cabTemplate.asTimestamp("DHTIPOPER"));

		cabTemplate.setProperty("NUNOTA", null);
		cabTemplate.setProperty("DHTIPOPER", null);
		cabTemplate.setProperty("DHTIPVENDA", null);
		cabTemplate.setProperty("NUMNOTA", new BigDecimal("0"));
		cabTemplate.setProperty("DTNEG", TimeUtils.getNow());
		cabTemplate.setProperty("DTENTSAI", TimeUtils.getNow());
		cabTemplate.setProperty("CODEMP", orcamento.getCodemp());
		cabTemplate.setProperty("CODPARC", orcamento.getCodparc());
		cabTemplate.setProperty("OBSERVACAO", orcamento.getObservacao());
		cabTemplate.setProperty("CODTIPVENDA", orcamento.getCodtipvenda());
		cabTemplate.setProperty("CODUSU", orcamento.getCodusu());
		cabTemplate.setProperty("CODVEND", orcamento.getCodvend());
		cabTemplate.setProperty("VLRNOTA", orcamento.getVlrnota());
		cabTemplate.setProperty("DESCTOT", orcamento.getDesctot());
		cabTemplate.setProperty("TIPMOV", topDoModelo.asString("TIPMOV"));
		//cabTemplate.setProperty("CODPARCTRANSP", new BigDecimal("0"));

		// duplica e cria a nova nunota
		DynamicVO novaNota = duplicar(cabDAO, cabTemplate);

		return novaNota;
	}

	@SuppressWarnings("unchecked")
	public static DynamicVO duplicar(JapeWrapper dao, DynamicVO modeloVO) throws Exception {
		FluidCreateVO fluidCreateVO = dao.create();
		Iterator<VOProperty> iterator = modeloVO.iterator();

		while (iterator.hasNext()) {
			VOProperty property = iterator.next();
			fluidCreateVO.set(property.getName(), property.getValue());
		}

		return fluidCreateVO.save();
	}

	/*
	 * private void adicionaItemPedido(DynamicVO nota, ArrayList<Item> itens,
	 * ArrayList<Produto> produtos) throws Exception {
	 * Collection<PrePersistEntityState> itensNota = new
	 * ArrayList<PrePersistEntityState>();
	 * 
	 * for (int i = 0; i < itens.size(); i++) { Item item = itens.get(i); Produto
	 * produto = produtos.get(i);
	 * 
	 * EntityFacade dwfFacade = EntityFacadeFactory.getDWFFacade(); DynamicVO itemVO
	 * = (DynamicVO)
	 * dwfFacade.getDefaultValueObjectInstance(DynamicEntityNames.ITEM_NOTA);
	 * 
	 * itemVO.setPrimaryKey(null); itemVO.setProperty("NUNOTA",
	 * nota.asBigDecimal("NUNOTA")); itemVO.setProperty("CODEMP",
	 * nota.asBigDecimal("CODEMP")); itemVO.setProperty("CODVEND",
	 * nota.asBigDecimal("CODVEND")); itemVO.setProperty("CODPROD",
	 * produto.getCodProd()); itemVO.setProperty("USOPROD", produto.getUsoProd());
	 * itemVO.setProperty("CODVOL", produto.getCodVol());
	 * itemVO.setProperty("CONTROLE",
	 * (StringUtils.getNullAsEmpty(produto.getControle()).isEmpty() ? " " :
	 * produto.getControle())); itemVO.setProperty("CODLOCALORIG",
	 * produto.getCodigoLocal()); itemVO.setProperty("QTDNEG",
	 * item.getQuantidade()); itemVO.setProperty("PERCDESC", BigDecimal.ZERO);
	 * itemVO.setProperty("VLRDESC", BigDecimal.ZERO); itemVO.setProperty("VLRUNIT",
	 * item.getPreco());
	 * 
	 * BigDecimal valorTotal = item.getPreco().multiply(item.getQuantidade());
	 * 
	 * itemVO.setProperty("VLRTOT", valorTotal);
	 * 
	 * PrePersistEntityState itemMontado = PrePersistEntityState.build(dwfFacade,
	 * DynamicEntityNames.ITEM_NOTA, itemVO);
	 * 
	 * itensNota.add(itemMontado);
	 * 
	 * } CACHelper sistema = new CACHelper();
	 * 
	 * sistema.incluirAlterarItem(nota.asBigDecimal("NUNOTA"), authInfo, itensNota,
	 * true); }
	 */

	@Override
	public void beforeCommit(TransactionContext arg0) throws Exception {
	}

	@Override
	public void beforeDelete(PersistenceEvent arg0) throws Exception {
	}

	@Override
	public void beforeInsert(PersistenceEvent arg0) throws Exception {
	}

	@Override
	public void beforeUpdate(PersistenceEvent arg0) throws Exception {
	}

	@Override
	public void afterDelete(PersistenceEvent arg0) throws Exception {
	}

}
