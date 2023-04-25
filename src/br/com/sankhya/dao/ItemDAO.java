package br.com.sankhya.dao;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;

import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.sql.NativeSql;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.wrapper.JapeFactory;
import br.com.sankhya.jape.wrapper.JapeWrapper;
import br.com.sankhya.model.Item;
import br.com.sankhya.modelcore.util.DynamicEntityNames;

/**
 * Essa classe faz conexão com o banco de dados para buscar dados relacionados à
 * classe Item.
 * 
 * @author Felipe S. Lopes (felipe.lopes@sankhya.com.br)
 * @since 2022-09-30
 * @version 1.1.0
 * 
 */
public class ItemDAO {

	/**
	 * Esse método obtém os dados de uma instância de registro de peça do orçamento
	 * e os insere na instância da classe Item.
	 * 
	 * @param pecaVO instância de registro de peça do orçamento;
	 * @return Instância da classe Item.
	 * @throws Exception
	 */
	public static Item read(DynamicVO pecaVO, String entityName) throws Exception {
		Item item = new Item();

		item.setCodoos(pecaVO.asBigDecimal("CODOOS"));
		item.setCodite(getCodite(pecaVO, entityName));
		item.setCodprod(pecaVO.asBigDecimal("CODPROD"));
		item.setCodlocalorig(new BigDecimal(12004));
		item.setPercdesc(pecaVO.asBigDecimal("PERCDESC"));
		item.setQtdneg(pecaVO.asBigDecimal("QTD"));
		item.setVlrtot(pecaVO.asBigDecimal("VLRTOT"));
		item.setVlracresc(coalesce(pecaVO, "VLRACRESC"));
		item.setVlrunit(coalesce(pecaVO, "VLRUNIT"));
		item.setVlrdesc(coalesce(pecaVO, "VLRDESC"));
		item.computedValues();

		return item;
	}

	/**
	 * Este método vincula a sequência do item criado ao orçamento atual.
	 * 
	 * @param jdbc       Conector do banco de dados.
	 * @param item       Entidade nota no qual possui as informações necessárias
	 *                   para a alteração.
	 * @param entityName nome da entidade alvo atual.
	 */
	public static void setSequencia(JdbcWrapper jdbc, Item item, String entityName) {
		NativeSql sql = new NativeSql(jdbc);

		sql.appendSql(" UPDATE ");
		sql.appendSql("    :ENTIDADE");
		sql.appendSql(" SET ");
		sql.appendSql("    SEQUENCIA = :SEQUENCIA");
		sql.appendSql(" WHERE");
		sql.appendSql("    CODOOS = :CODOOS");
		sql.appendSql("    AND CODITE = :CODITE");

		sql.setNamedParameter("ENTIDADE", entityName);
		sql.setNamedParameter("SEQUENCIA", item.getSequencia());
		sql.setNamedParameter("CODOOS", item.getCodoos());
		sql.setNamedParameter("CODITE", item.getCodite());

		try {
			sql.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Erro na execução da consulta SQL.");
		}
	}

	/**
	 * Método que busca e retorna a instância de um registro da tabela de itens
	 * (TGFITE).
	 * 
	 * @param nunota    Número único do registro do pedido do item a ser buscado.
	 * @param sequencia Código da peça do Orçamento/Ordem de Serviço que será
	 *                  buscado no item.
	 * @return DynamicVO iteVO instância de um registro da tabela de itens (TGFITE).
	 */
	public static DynamicVO getItemVO(BigDecimal nunota, BigDecimal sequencia) {
		JapeWrapper iteDAO = JapeFactory.dao(DynamicEntityNames.ITEM_NOTA);
		DynamicVO iteVO = null;
		try {
			iteVO = iteDAO.findOne(" NUNOTA = " + nunota + " AND SEQUENCIA = " + sequencia);
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Erro na pesquisa do registro do item.");
		}

		return iteVO;
	}

	/**
	 * Este método retorna o código do item de orçamento de acordo com o a entidade
	 * alvo atual.
	 * 
	 * @param pecaVO     instância do item do orçamento.
	 * @param entityName nome da entidade alvo atual.
	 * @return BigDecimal código do item de orçamento.
	 */
	public static BigDecimal getCodite(DynamicVO pecaVO, String entityName) {
		return entityName.equals("AD_OOSITE") ? pecaVO.asBigDecimal("CODITE") : pecaVO.asBigDecimal("CODITESERV");
	}

	/**
	 * Esse método busca o porcentual de lucro de um item.
	 * 
	 * @param jdbc   Conector do banco de dados.
	 * @param pecaVO instância de uma peça.
	 * @return o porcentual de lucro buscado pela função do banco de dados.
	 */
	private static BigDecimal getProfit(JdbcWrapper jdbc, DynamicVO pecaVO) {
		NativeSql sql = new NativeSql(jdbc);
		BigDecimal profit = null;

		sql.appendSql("SELECT GET_LUCRO_ITEM_OS_ICCAP(:CODOOS, :CODITE) ");
		sql.appendSql("FROM DUAL ");

		sql.setNamedParameter("CODOOS", pecaVO.asBigDecimal("CODOOS"));
		sql.setNamedParameter("CODITE", pecaVO.asBigDecimal("CODITE"));

		ResultSet result;
		try {
			result = sql.executeQuery();
			if (result.next())
				profit = result.getBigDecimal(1);
			result.close();
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("Erro na busca da consulta SQL.");
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Erro na execução da consulta SQL.");
		}

		return profit;
	}

	/**
	 * Esse método busca o porcentual da margem de contribuição de um item.
	 * 
	 * @param jdbc   Conector do banco de dados.
	 * @param pecaVO instância de uma peça.
	 * @return o porcentual da margem de contribuição buscado pela função do banco
	 *         de dados.
	 */
	private static BigDecimal getContributionMargin(JdbcWrapper jdbc, DynamicVO pecaVO) {
		NativeSql sql = new NativeSql(jdbc);
		BigDecimal contMargin = null;

		sql.appendSql("SELECT GET_MARG_CONTRIB_ITEM_OS_ICCAP(:CODOOS, :CODITE) ");
		sql.appendSql("FROM DUAL ");

		sql.setNamedParameter("CODOOS", pecaVO.asBigDecimal("CODOOS"));
		sql.setNamedParameter("CODITE", pecaVO.asBigDecimal("CODITE"));

		ResultSet result;
		try {
			result = sql.executeQuery();
			if (result.next())
				contMargin = result.getBigDecimal(1);
			result.close();
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("Erro na busca da consulta SQL.");
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Erro na execução da consulta SQL.");
		}

		return contMargin;
	}

	/**
	 * Esse método atualiza o campo de percentual de lucro na tabela de peças.
	 * 
	 * @param jdbc   Conector do banco de dados.
	 * @param pecaVO instância de uma peça.
	 * @param entityName nome da entidade alvo atual.
	 */
	public static void updateProfit(JdbcWrapper jdbc, DynamicVO pecaVO, String entityName) {
		BigDecimal perlucro = getProfit(jdbc, pecaVO);
		BigDecimal margcontrib = getContributionMargin(jdbc, pecaVO);

		NativeSql sql = new NativeSql(jdbc);

		sql.appendSql(" UPDATE ");
		sql.appendSql("    :ENTIDADE ");
		sql.appendSql(" SET ");
		sql.appendSql("    PERLUCRO = :PERLUCRO, ");
		sql.appendSql("    MARGCONTRIB = :MARGCONTRIB  ");
		sql.appendSql(" WHERE  ");
		sql.appendSql("    CODOOS = :CODOOS ");
		sql.appendSql("    AND CODITE = :CODITE");

		sql.setNamedParameter("ENTIDADE", entityName);
		sql.setNamedParameter("PERLUCRO", perlucro);
		sql.setNamedParameter("MARGCONTRIB", margcontrib);
		sql.setNamedParameter("CODOOS", pecaVO.asBigDecimal("CODOOS"));
		sql.setNamedParameter("CODITE", pecaVO.asBigDecimal("CODITE"));

		try {
			sql.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Erro na execução da consulta SQL.");
		}
	}

	/**
	 * Esse retorna o valor de um campo buscado da instância de um registro da peça
	 * ou, caso este seja nulo, retorna 0 (zero).
	 * 
	 * @param iteVO instância de um registro da peça.
	 * @param field campo a ser buscado.
	 * @return retorna o valor do campo buscado ou 0 (zero).
	 */
	private static BigDecimal coalesce(DynamicVO iteVO, String field) {
		return iteVO.asBigDecimal(field) == null ? BigDecimal.ZERO : iteVO.asBigDecimal(field);
	}
}
