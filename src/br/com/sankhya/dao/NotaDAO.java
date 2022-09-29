package br.com.sankhya.dao;

import java.math.BigDecimal;
import java.sql.ResultSet;

import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.sql.NativeSql;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.model.Nota;

public class NotaDAO {

	public static Nota read(PersistenceEvent ctx, JdbcWrapper jdbc) throws Exception {
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
