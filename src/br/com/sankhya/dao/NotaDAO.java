package br.com.sankhya.dao;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;

import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.sql.NativeSql;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.wrapper.JapeFactory;
import br.com.sankhya.jape.wrapper.JapeWrapper;
import br.com.sankhya.model.Nota;
import br.com.sankhya.modelcore.util.DynamicEntityNames;

/**
 * Essa classe faz conex�o com o banco de dados para buscar dados relacionados �
 * classe Nota.
 * 
 * @author Felipe S. Lopes (felipe.lopes@sankhya.com.br)
 * @since 2022-09-28
 * @version 1.0.0
 * 
 */
public class NotaDAO {

	/**
	 * L� e busca um registro da tabela AD_CADOOS e alimenta os dados em uma
	 * inst�ncia de Nota.
	 * 
	 * @param cabosVO Inst�ncia do registro de or�amento.
	 * @param jdbc    Conector do banco de dados.
	 * @return Nota uma inst�ncia de nota.
	 * @throws Exception
	 */
	public static Nota read(DynamicVO cabosVO, JdbcWrapper jdbc) throws Exception {

		Nota nota = new Nota();

		nota.setCodos(cabosVO.asBigDecimal("CODOOS"));
		nota.setNunota(getNunota(cabosVO, jdbc));
		nota.setCodemp(cabosVO.asBigDecimal("CODEMP"));
		nota.setCodparc(cabosVO.asBigDecimal("CODPARC"));
		nota.setCodtipvenda(cabosVO.asBigDecimal("CODTIPVENDA"));
		nota.setCodusu(cabosVO.asBigDecimal("CODUSU"));
		nota.setCodvend(cabosVO.asBigDecimal("CODVEND"));
		nota.setTipolancamento(cabosVO.asString("TIPOLANCAMENTO"));
		nota.setObservacao(cabosVO.asString("OBSERVACAO"));
		nota.setVlrnota(cabosVO.asBigDecimal("VLRTOTGERAL"));
		nota.setDesctot(cabosVO.asBigDecimal("DESCTOTAL"));
		nota.setCodmotivoabert(cabosVO.asBigDecimal("CODMOTIVOABERT"));
		
		ArrayList<Object> attributes = getOperVenda(nota.getNunota(), jdbc);
		
		nota.setCodtipoper((BigDecimal)attributes.get(0));
		nota.setDhtipoper((Timestamp)attributes.get(2));
		nota.setDhtipvenda((Timestamp)attributes.get(3));

		return nota;
	}

	/**
	 * Este m�todo busca a inst�ncia do pedido equivalente ao Or�amento/Ordem de
	 * Servi�o com o c�digo passado.
	 * 
	 * @param codoos C�digo da OS passado para realizar a busca do registro do
	 *               pedido.
	 * @return DynamicVO cabVO inst�ncia do registro da Nota.
	 */
	@Deprecated
	public static DynamicVO getCabVO(BigDecimal codoos) {
		JapeWrapper cabDAO = JapeFactory.dao(DynamicEntityNames.CABECALHO_NOTA);
		DynamicVO cabVO = null;
		try {
			cabVO = cabDAO.findOne(" AD_CODOS = " + codoos + " AND TIPMOV = 'P'");
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Erro na pesquisa do registro da nota.");
		}

		return cabVO;
	}

	private static BigDecimal getNunota(DynamicVO cabosVO, JdbcWrapper jdbc) {
		return cabosVO.asBigDecimal("NUNOTA") == null ? getNunotaTemplate(jdbc, cabosVO)
				: cabosVO.asBigDecimal("NUNOTA");
	}

	/**
	 * Este m�todo busca a inst�ncia do Or�amento/Ordem de Servi�o com o c�digo
	 * passado.
	 * 
	 * @param codoos C�digo da OS passado para realizar a busca do registro do
	 *               pedido.
	 * @return DynamicVO oscabVO inst�ncia do registro do Or�amento/OS.
	 */
	public static DynamicVO getCabOSVO(BigDecimal codoos) {
		JapeWrapper oscabDAO = JapeFactory.dao("AD_OOSCAB");
		DynamicVO oscabVO = null;
		try {
			oscabVO = oscabDAO.findByPK(codoos);
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Erro na pesquisa do registro do or�amento de ordem de servi�o.");
		}
		return oscabVO;
	}

	/**
	 * Este m�todo vincula o nro. da nota criada ao or�amento atual.
	 * 
	 * @param jdbc Conector do banco de dados.
	 * @param nota Entidade nota no qual possui as informa��es necess�rias para a
	 *             altera��o.
	 */
	public static void setNunota(JdbcWrapper jdbc, Nota nota) {
		NativeSql sql = new NativeSql(jdbc);

		sql.appendSql(" UPDATE ");
		sql.appendSql("    AD_OOSCAB");
		sql.appendSql(" SET ");
		sql.appendSql("    NUNOTA = :NUNOTA");
		sql.appendSql(" WHERE");
		sql.appendSql("    CODOOS = :CODOOS");

		sql.setNamedParameter("NUNOTA", nota.getNunota());
		sql.setNamedParameter("CODOOS", nota.getCodos());

		try {
			sql.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Erro na execu��o da consulta SQL.");
		}
	}

	/**
	 * Essa classe busca o nunota de uma nota modelo.
	 * 
	 * @param jdbc Conector do banco de dados.
	 * @param nota inst�ncia de uma nota
	 * @return o NUNOTA de uma nota modelo.
	 */
	public static BigDecimal getNunotaTemplate(JdbcWrapper jdbc, DynamicVO cabosVO) {
		NativeSql sql = new NativeSql(jdbc);
		BigDecimal nunotaTemplate = null;

		sql.appendSql("SELECT  ");
		sql.appendSql("    NUNOTAPEDIDO ");
		sql.appendSql("FROM ");
		sql.appendSql("    AD_MOTIVOABERT ");
		sql.appendSql("WHERE ");
		sql.appendSql("    CODMOTIVOABERT = :CODMOTIVOABERT ");

		sql.setNamedParameter("CODMOTIVOABERT", cabosVO.asBigDecimal("CODMOTIVOABERT"));

		ResultSet result;
		try {
			result = sql.executeQuery();
			if (result.next())
				nunotaTemplate = result.getBigDecimal("NUNOTAPEDIDO");
			result.close();
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("Erro na busca da consulta SQL.");
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Erro na execu��o da consulta SQL.");
		}

		return nunotaTemplate;
	}

	/**
	 * Busca os c�digos e datas do tipo de opera��o e do tipo de venda de um
	 * or�amento.
	 * 
	 * @param nunota c�digo do motivo de abertura.
	 * @param jdbc   Conector do banco de dados.
	 * @return os c�digos de datas do tipo de opera��o e venda do or�amento.
	 * @throws Exception
	 */
	private static ArrayList<Object> getOperVenda(BigDecimal nunota, JdbcWrapper jdbc) throws Exception {
		ArrayList<Object> list = new ArrayList<>();
		NativeSql sql = new NativeSql(jdbc);

		sql.appendSql(" SELECT ");
		sql.appendSql("    CODTIPOPER, ");
		sql.appendSql("    DHTIPOPER, ");
		sql.appendSql("    CODTIPVENDA, ");
		sql.appendSql("    DHTIPVENDA ");
		sql.appendSql(" FROM ");
		sql.appendSql("    TGFCAB");
		sql.appendSql(" WHERE ");
		sql.appendSql("    NUNOTA = :NUNOTA ");

		sql.setNamedParameter("NUNOTA", nunota);

		ResultSet result = sql.executeQuery();

		if (result.next()) {
			list.add(result.getBigDecimal("CODTIPOPER"));
			list.add(result.getBigDecimal("CODTIPVENDA"));
			list.add(result.getTimestamp("DHTIPOPER"));
			list.add(result.getTimestamp("DHTIPVENDA"));
		}

		result.close();

		return list;

	}
}
