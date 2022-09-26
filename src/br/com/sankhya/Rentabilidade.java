package br.com.sankhya;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.util.Date;
import java.util.Iterator;

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
import br.com.sankhya.modelcore.util.DynamicEntityNames;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;

/**
 * Este programa realiza o espelhamento dos valores do cabeçalho da tela de
 * Ordens de Serviços para o cabeçalho da nota.
 * 
 * @author Felipe S. Lopes (felipe.lopes@sankhya.com.br)
 * @since 2022-09-22
 * @version 0.1.0
 * 
 */
public class Rentabilidade implements EventoProgramavelJava {

	@Override
	public void afterInsert(PersistenceEvent ctx) throws Exception {
		// TODO Quando um orçamento da tela de Ordens de Serviço for criado
		// criar uma nota com os valores do cabeçalho deste orçamento.
		DynamicVO ordemVO = (DynamicVO) ctx.getVo();

		String tipolancamento = ordemVO.asString("TIPOLANCAMENTO");

		if (tipolancamento.equals("O")) {
			SessionHandle hnd = null;
			JdbcWrapper jdbc = null;

			try {
				hnd = JapeSession.open();
				EntityFacade dwfEntityFacade = EntityFacadeFactory.getDWFFacade();
				jdbc = dwfEntityFacade.getJdbcWrapper();

				NativeSql sql = new NativeSql(jdbc);
				sql.appendSql("SELECT M.CODTIPOPERORC\r\n");
				sql.appendSql("FROM AD_MOTIVOABERT M\r\n");
				sql.appendSql("JOIN TGFTOP T ON T.CODTIPOPER = M.CODTIPOPERORC\r\n");
				sql.appendSql("WHERE M.CODMOTIVOABERT = 1\r\n"); // Alterar dinâmico
				sql.appendSql("AND ROWNUM = 1\r\n");
				sql.appendSql("ORDER BY DHALTER DESC");
				
				ResultSet result = sql.executeQuery();
				BigDecimal codtipoper = null;
				
				if (result.next())
					codtipoper = result.getBigDecimal("CODTIPOPERORC");
				
				result.close();


				BigDecimal nunotaTemplate = null;
				NativeSql sql2 = new NativeSql(jdbc);
				sql2.appendSql("SELECT NUNOTA ");
				sql2.appendSql("FROM TGFCAB ");
				sql2.appendSql("WHERE CODTIPOPER = :CODTIPOPER AND ROWNUM = 1 ");
				sql2.appendSql("ORDER BY DTNEG DESC");
				sql2.setNamedParameter("CODTIPOPER", codtipoper);
				
				ResultSet result2 = sql.executeQuery();
				
				if (result2.next())
					nunotaTemplate = result2.getBigDecimal("NUNOTA");

				result2.close();

				createNovaNota(jdbc, nunotaTemplate);


			} catch (Exception e) {
				e.printStackTrace();
				//MGEModelException.throwMe(e);
			} finally {
				JapeSession.close(hnd);
			}

		}

		// TODO Se for um serviço já faturado, vai verificar qual o códgigo de OS
		// de origem, buscar o nro. único da nota dessa OS e fazer essa nota ser
		// faturada.

	}

	@Override
	public void afterUpdate(PersistenceEvent arg0) throws Exception {
		// TODO Quando um orçamento ou um serviço for alterado

	}

	public static DynamicVO createNovaNota(JdbcWrapper jdbc, BigDecimal nunotaTemplate) throws Exception {
		boolean teste = true;
		JapeWrapper cabDAO = JapeFactory.dao(DynamicEntityNames.CABECALHO_NOTA);
		
		if (teste)
			throw new Exception("CODOPER: ");
		final JapeWrapper tipoOperacaoDAO = JapeFactory.dao(DynamicEntityNames.TIPO_OPERACAO);

		if (nunotaTemplate == null)
			throw new Exception("Nota modelo de operação de pedido de venda não existe ou não "
					+ "foi cadastrada corretamentre na tela de preferencias panorama");

		final DynamicVO cabTemplate = cabDAO.findByPK(nunotaTemplate);

		if (cabTemplate == null)
			throw new Exception("Nota modelo de operação de pedido de venda não existe ou não foi "
					+ "cadastrada corretamentre na tela de preferencias panorama");

		final DynamicVO topDoModelo = tipoOperacaoDAO.findByPK(cabTemplate.asBigDecimal("CODTIPOPER"),
				cabTemplate.asTimestamp("DHTIPOPER"));

		cabTemplate.setProperty("NUNOTA", null);
		cabTemplate.setProperty("DHTIPOPER", null);
		cabTemplate.setProperty("DHTIPVENDA", null);
		cabTemplate.setProperty("TIPMOV", topDoModelo.asString("TIPMOV"));
		cabTemplate.setProperty("DTNEG", new Date());
		cabTemplate.setProperty("DTENTSAI", new Date());
		cabTemplate.setProperty("CODEMP", BigDecimal.valueOf(1));
		cabTemplate.setProperty("CODPARC", BigDecimal.valueOf(158));
		cabTemplate.setProperty("NUMNOTA", new BigDecimal("0"));
		cabTemplate.setProperty("VLRNOTA", BigDecimal.valueOf(2.80));
		cabTemplate.setProperty("OBSERVACAO", "");
		cabTemplate.setProperty("CIF_FOB", "S");
		cabTemplate.setProperty("CODPARCTRANSP", new BigDecimal("0"));
		cabTemplate.setProperty("QTDVOL", new BigDecimal("0"));

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
