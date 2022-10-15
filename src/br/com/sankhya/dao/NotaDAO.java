package br.com.sankhya.dao;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;

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

		nota.setCodemp(cabosVO.asBigDecimal("CODEMP"));
		nota.setCodos(cabosVO.asBigDecimal("CODOOS"));
		nota.setCodparc(cabosVO.asBigDecimal("CODPARC"));
		nota.setCodtipvenda(cabosVO.asBigDecimal("CODTIPVENDA"));
		nota.setCodusu(cabosVO.asBigDecimal("CODUSU"));
		nota.setCodvend(cabosVO.asBigDecimal("CODVEND"));
		nota.setTipolancamento(cabosVO.asString("TIPOLANCAMENTO"));
		nota.setObservacao(cabosVO.asString("OBSERVACAO"));
		nota.setVlrnota(cabosVO.asBigDecimal("VLRTOTGERAL"));
		nota.setDesctot(cabosVO.asBigDecimal("DESCTOTAL"));
		nota.setCodmotivoabert(cabosVO.asBigDecimal("CODMOTIVOABERT"));
		nota.setCodtipoper(readCodtipoper(nota.getCodmotivoabert(), jdbc));

		return nota;
	}

	/**
	 * L� e busca um registro da tabela AD_CADOOS e alimenta os dados em uma
	 * inst�ncia de Nota com um NUNOTA.
	 * 
	 * @param cabosVO Inst�ncia do registro de or�amento.
	 * @param jdbc    Conector do banco de dados.
	 * @return Nota uma inst�ncia de nota.
	 * @throws Exception
	 */
	public static Nota readOrder(DynamicVO cabosVO, JdbcWrapper jdbc) throws Exception {

		Nota nota = new Nota();

		nota.setCodemp(cabosVO.asBigDecimal("CODEMP"));
		nota.setCodos(cabosVO.asBigDecimal("CODOOS"));
		nota.setCodparc(cabosVO.asBigDecimal("CODPARC"));
		nota.setCodtipvenda(cabosVO.asBigDecimal("CODTIPVENDA"));
		nota.setCodusu(cabosVO.asBigDecimal("CODUSU"));
		nota.setCodvend(cabosVO.asBigDecimal("CODVEND"));
		nota.setTipolancamento(cabosVO.asString("TIPOLANCAMENTO"));
		nota.setObservacao(cabosVO.asString("OBSERVACAO"));
		nota.setVlrnota(cabosVO.asBigDecimal("VLRTOTGERAL"));
		nota.setDesctot(cabosVO.asBigDecimal("DESCTOTAL"));
		nota.setCodmotivoabert(cabosVO.asBigDecimal("CODMOTIVOABERT"));
		nota.setCodtipoper(readCodtipoper(nota.getCodmotivoabert(), jdbc));
		nota.setNunota(getCabVO(cabosVO.asBigDecimal("CODOOS")).asBigDecimal("NUNOTA"));

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
	public static DynamicVO getCabVO(BigDecimal codoos) {
		JapeWrapper cabDAO = JapeFactory.dao(DynamicEntityNames.CABECALHO_NOTA);
		DynamicVO cabVO = null;
		try {
			cabVO = cabDAO.findOne(" AD_CODOS = " + codoos);
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Erro na pesquisa do registro da nota.");
		}

		return cabVO;
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
	 * Essa classe busca o nunota de uma nota modelo.
	 * 
	 * @param jdbc Conector do banco de dados.
	 * @param nota inst�ncia de uma nota
	 * @return o NUNOTA de uma nota modelo.
	 */
	public static BigDecimal readNunotaTemplate(JdbcWrapper jdbc, Nota nota) {
		NativeSql sql = new NativeSql(jdbc);
		BigDecimal nunotaTemplate = null;

		sql.appendSql("SELECT NUNOTA ");
		sql.appendSql("FROM TGFCAB ");
		sql.appendSql("WHERE CODTIPOPER = :CODTIPOPER AND ROWNUM = 1 ");
		sql.appendSql("ORDER BY DTNEG DESC");
		sql.setNamedParameter("CODTIPOPER", nota.getCodtipoper());

		ResultSet result;
		try {
			result = sql.executeQuery();
			if (result.next())
				nunotaTemplate = result.getBigDecimal("NUNOTA");
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
	 * Busca o c�digo do tipo de opera��o de um or�amento de acordo com o seu motivo
	 * de abertura.
	 * 
	 * @param codmotivoabert c�digo do motivo de abertura.
	 * @param jdbc           Conector do banco de dados.
	 * @return o c�digo do tipo de opera��o do or�amento.
	 * @throws Exception
	 */
	private static BigDecimal readCodtipoper(BigDecimal codmotivoabert, JdbcWrapper jdbc) throws Exception {
		NativeSql sql = new NativeSql(jdbc);

		sql.appendSql("SELECT M.CODTIPOPERORC ");
		sql.appendSql("FROM AD_MOTIVOABERT M ");
		sql.appendSql("JOIN TGFTOP T ON T.CODTIPOPER = M.CODTIPOPERORC ");
		sql.appendSql("WHERE M.CODMOTIVOABERT = :CODMOTIVOABERT ");
		sql.appendSql("AND ROWNUM = 1 ");
		sql.appendSql("ORDER BY DHALTER DESC");

		sql.setNamedParameter("CODMOTIVOABERT", codmotivoabert);

		ResultSet result = sql.executeQuery();

		if (result.next())
			return result.getBigDecimal("CODTIPOPERORC");

		result.close();

		return BigDecimal.ZERO;

	}

}
