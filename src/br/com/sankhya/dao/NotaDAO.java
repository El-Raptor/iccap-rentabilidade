package br.com.sankhya.dao;

import java.math.BigDecimal;
import java.sql.ResultSet;

import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.sql.NativeSql;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.wrapper.JapeFactory;
import br.com.sankhya.jape.wrapper.JapeWrapper;
import br.com.sankhya.model.Nota;
import br.com.sankhya.modelcore.util.DynamicEntityNames;

public class NotaDAO {

	public static Nota read(DynamicVO cabosVO, JdbcWrapper jdbc) throws Exception {
		// TODO: Colocar o DynamicVO como parâmetro.
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
	
	public static Nota readOrder(DynamicVO cabosVO, JdbcWrapper jdbc) throws Exception {
		// TODO: Colocar o DynamicVO como parâmetro.
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
		nota.setNunota(getNunota(jdbc, cabosVO.asBigDecimal("CODOOS")));

		return nota;
	}

	private static BigDecimal getNunota(JdbcWrapper jdbc, BigDecimal codos) throws Exception {
		NativeSql sql = new NativeSql(jdbc);

		sql.appendSql("SELECT NUNOTA ");
		sql.appendSql("FROM TGFCAB ");
		sql.appendSql("WHERE AD_CODOS = :CODOS");

		sql.setNamedParameter("CODOS", codos);

		ResultSet result = sql.executeQuery();

		if (result.next())
			return result.getBigDecimal("NUNOTA");

		result.close();

		return null;
	}

	/**
	 * Este método busca a instância do pedido equivalente ao Orçamento/Ordem de
	 * Serviço com o código passado.
	 * 
	 * @param codoos Código da OS passado para realizar a busca do registro do
	 *               pedido.
	 * @return DynamicVO cabVO instância do registro da Nota.
	 */
	public static DynamicVO getCabVO(BigDecimal codoos) {
		JapeWrapper cabDAO = JapeFactory.dao(DynamicEntityNames.CABECALHO_NOTA);
		DynamicVO cabVO = null;
		try {
			cabVO = cabDAO.findOne(" AD_CODOS = " + codoos);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return cabVO;
	}

	/**
	 * Este método busca a instância do Orçamento/Ordem de Serviço com o código
	 * passado.
	 * 
	 * @param codoos Código da OS passado para realizar a busca do registro do
	 *               pedido.
	 * @return DynamicVO oscabVO instância do registro do Orçamento/OS.
	 */
	public static DynamicVO getCabOSVO(BigDecimal codoos) {
		JapeWrapper oscabDAO = JapeFactory.dao("AD_OOSCAB");
		DynamicVO oscabVO = null;
		try {
			oscabVO = oscabDAO.findByPK(codoos);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return oscabVO;
	}

	public static BigDecimal readNunota(JdbcWrapper jdbc, Nota nota) {
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
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return nunotaTemplate;
	}

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
