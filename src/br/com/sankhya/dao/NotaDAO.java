package br.com.sankhya.dao;

import java.math.BigDecimal;
import java.sql.ResultSet;

import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.sql.NativeSql;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.wrapper.JapeFactory;
import br.com.sankhya.jape.wrapper.JapeWrapper;
import br.com.sankhya.model.Nota;
import br.com.sankhya.modelcore.util.DynamicEntityNames;

public class NotaDAO {

	public static Nota read(PersistenceEvent ctx, JdbcWrapper jdbc) throws Exception {
		// TODO: Colocar o DynamicVO como par�metro.
		DynamicVO notaVO = (DynamicVO) ctx.getVo();
		Nota nota = new Nota();

		nota.setCodemp(notaVO.asBigDecimal("CODEMP"));
		nota.setCodos(notaVO.asBigDecimal("CODOOS"));
		nota.setCodparc(notaVO.asBigDecimal("CODPARC"));
		nota.setCodtipvenda(notaVO.asBigDecimal("CODTIPVENDA"));
		nota.setCodusu(notaVO.asBigDecimal("CODUSU"));
		nota.setCodvend(notaVO.asBigDecimal("CODVEND"));
		nota.setTipolancamento(notaVO.asString("TIPOLANCAMENTO"));
		nota.setObservacao(notaVO.asString("OBSERVACAO"));
		nota.setVlrnota(notaVO.asBigDecimal("VLRTOTGERAL"));
		nota.setDesctot(notaVO.asBigDecimal("DESCTOTAL"));
		nota.setCodmotivoabert(notaVO.asBigDecimal("CODMOTIVOABERT"));
		nota.setCodtipoper(readCodtipoper(nota.getCodmotivoabert(), jdbc));

		return nota;
	}
	
	public static BigDecimal readNunota (JdbcWrapper jdbc, BigDecimal codos) throws Exception {
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
			// TODO Auto-generated catch block
			e.printStackTrace();
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return oscabVO;
	}

	private static BigDecimal readCodtipoper(BigDecimal codmotivoabert, JdbcWrapper jdbc)
			throws Exception {
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
