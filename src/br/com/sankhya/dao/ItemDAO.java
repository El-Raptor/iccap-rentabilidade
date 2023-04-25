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
 * Essa classe faz conex�o com o banco de dados para buscar dados relacionados �
 * classe Item.
 * 
 * @author Felipe S. Lopes (felipe.lopes@sankhya.com.br)
 * @since 2022-09-30
 * @version 1.1.0
 * 
 */
public class ItemDAO {

	/**
	 * Esse m�todo obt�m os dados de uma inst�ncia de registro de pe�a do or�amento
	 * e os insere na inst�ncia da classe Item.
	 * 
	 * @param pecaVO inst�ncia de registro de pe�a do or�amento;
	 * @return Inst�ncia da classe Item.
	 * @throws Exception
	 */
	public static Item read(DynamicVO pecaVO) throws Exception {
		Item item = new Item();

		item.setCodoos(pecaVO.asBigDecimal("CODOOS"));
		item.setCodite(pecaVO.asBigDecimal("CODITE"));
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
	 * Este m�todo vincula a sequ�ncia do item criado ao or�amento atual.
	 * 
	 * @param jdbc Conector do banco de dados.
	 * @param item Entidade nota no qual possui as informa��es necess�rias para a
	 *             altera��o.
	 */
	public static void setSequencia(JdbcWrapper jdbc, Item item) {
		NativeSql sql = new NativeSql(jdbc);

		sql.appendSql(" UPDATE ");
		sql.appendSql("    AD_OOSITE");
		sql.appendSql(" SET ");
		sql.appendSql("    SEQUENCIA = :SEQUENCIA");
		sql.appendSql(" WHERE");
		sql.appendSql("    CODOOS = :CODOOS");
		sql.appendSql("    AND CODITE = :CODITE");

		sql.setNamedParameter("SEQUENCIA", item.getSequencia());
		sql.setNamedParameter("CODOOS", item.getCodoos());
		sql.setNamedParameter("CODITE", item.getCodite());

		try {
			sql.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Erro na execu��o da consulta SQL.");
		}
	}

	/**
	 * M�todo que busca e retorna a inst�ncia de um registro da tabela de itens
	 * (TGFITE).
	 * 
	 * @param nunota N�mero �nico do registro do pedido do item a ser buscado.
	 * @param sequencia C�digo da pe�a do Or�amento/Ordem de Servi�o que ser� buscado
	 *               no item.
	 * @return DynamicVO iteVO inst�ncia de um registro da tabela de itens (TGFITE).
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
	 * Esse m�todo busca o porcentual de lucro de um item.
	 * 
	 * @param jdbc   Conector do banco de dados.
	 * @param pecaVO inst�ncia de uma pe�a.
	 * @return o porcentual de lucro buscado pela fun��o do banco de dados.
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
			System.out.println("Erro na execu��o da consulta SQL.");
		}

		return profit;
	}
	
	/**
	 * Esse m�todo busca o porcentual da margem de contribui��o de um item.
	 * 
	 * @param jdbc   Conector do banco de dados.
	 * @param pecaVO inst�ncia de uma pe�a.
	 * @return o porcentual da margem de contribui��o buscado pela fun��o do banco de dados.
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
			System.out.println("Erro na execu��o da consulta SQL.");
		}

		return contMargin;
	}

	/**
	 * Esse m�todo atualiza o campo de percentual de lucro na tabela de pe�as.
	 * 
	 * @param jdbc   Conector do banco de dados.
	 * @param pecaVO inst�ncia de uma pe�a.
	 */
	public static void updateProfit(JdbcWrapper jdbc, DynamicVO pecaVO) {
		BigDecimal perlucro = getProfit(jdbc, pecaVO);
		BigDecimal margcontrib = getContributionMargin(jdbc, pecaVO);

		NativeSql sql = new NativeSql(jdbc);

		sql.appendSql("UPDATE AD_OOSITE ");
		sql.appendSql("SET PERLUCRO = :PERLUCRO, MARGCONTRIB = :MARGCONTRIB ");
		sql.appendSql("WHERE CODOOS = :CODOOS AND CODITE = :CODITE");

		sql.setNamedParameter("PERLUCRO", perlucro);
		sql.setNamedParameter("MARGCONTRIB", margcontrib);
		sql.setNamedParameter("CODOOS", pecaVO.asBigDecimal("CODOOS"));
		sql.setNamedParameter("CODITE", pecaVO.asBigDecimal("CODITE"));

		try {
			sql.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Erro na execu��o da consulta SQL.");
		}
	}

	/**
	 * Esse retorna o valor de um campo buscado da inst�ncia de um registro da pe�a
	 * ou, caso este seja nulo, retorna 0 (zero).
	 * 
	 * @param iteVO inst�ncia de um registro da pe�a.
	 * @param field campo a ser buscado.
	 * @return retorna o valor do campo buscado ou 0 (zero).
	 */
	private static BigDecimal coalesce(DynamicVO iteVO, String field) {
		return iteVO.asBigDecimal(field) == null ? BigDecimal.ZERO : iteVO.asBigDecimal(field);
	}
}
