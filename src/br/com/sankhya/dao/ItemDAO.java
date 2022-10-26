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
	 * @param iteVO instância de registro de peça do orçamento;
	 * @return Instância da classe Item.
	 * @throws Exception
	 */
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
			e.printStackTrace();
			System.out.println("Erro na pesquisa do registro do item.");
		}

		return iteVO;
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
	 * @return o porcentual da margem de contribuição buscado pela função do banco de dados.
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
