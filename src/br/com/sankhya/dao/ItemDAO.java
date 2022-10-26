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
 * @version 1.0.0
 * 
 */
public class ItemDAO {

	/**
	 * Esse m�todo obt�m os dados de uma inst�ncia de registro de pe�a do or�amento
	 * e os insere na inst�ncia da classe Item.
	 * 
	 * @param iteVO inst�ncia de registro de pe�a do or�amento;
	 * @return Inst�ncia da classe Item.
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
	 * M�todo que busca e retorna a inst�ncia de um registro da tabela de itens
	 * (TGFITE).
	 * 
	 * @param nunota N�mero �nico do registro do pedido do item a ser buscado.
	 * @param codite C�digo da pe�a do Or�amento/Ordem de Servi�o que ser� buscado
	 *               no item.
	 * @return DynamicVO iteVO inst�ncia de um registro da tabela de itens (TGFITE).
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
	 * Essa classe busca o nunota de uma nota modelo.
	 * 
	 * @param jdbc Conector do banco de dados.
	 * @param nota inst�ncia de uma nota
	 * @return o NUNOTA de uma nota modelo.
	 */
	public static BigDecimal getProfit(JdbcWrapper jdbc, DynamicVO pecaVO) {
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
	
	public static void updateProfit(JdbcWrapper jdbc, DynamicVO pecaVO) {
		BigDecimal perlucro = getProfit(jdbc, pecaVO);
		
		NativeSql sql = new NativeSql(jdbc);
		
		sql.appendSql("UPDATE AD_OOSITE ");
		sql.appendSql("SET PERLUCRO = " + perlucro);
		sql.appendSql(" WHERE CODOOS = :CODOOS AND CODITE = :CODITE");
		
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
