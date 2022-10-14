package br.com.sankhya;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.util.Iterator;

import com.sankhya.util.TimeUtils;

import br.com.sankhya.extensions.eventoprogramavel.EventoProgramavelJava;
import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.bmp.PersistentLocalEntity;
import br.com.sankhya.jape.core.JapeSession;
import br.com.sankhya.jape.core.JapeSession.SessionHandle;
import br.com.sankhya.jape.dao.EntityDAO;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.event.TransactionContext;
import br.com.sankhya.jape.sql.NativeSql;
import br.com.sankhya.jape.util.JapeSessionContext;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.vo.PrePersistEntityState;
import br.com.sankhya.jape.vo.VOProperty;
import br.com.sankhya.jape.wrapper.JapeFactory;
import br.com.sankhya.jape.wrapper.JapeWrapper;
import br.com.sankhya.jape.wrapper.fluid.FluidCreateVO;
import br.com.sankhya.model.Nota;
import br.com.sankhya.modelcore.auth.AuthenticationInfo;
import br.com.sankhya.modelcore.comercial.centrais.CACHelper;
import br.com.sankhya.modelcore.util.DynamicEntityNames;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;
import br.com.sankhya.modelcore.util.ListenerParameters;

/**
 * Este programa realiza o espelhamento dos valores do cabeçalho da tela de
 * Ordens de Serviços para o cabeçalho da nota.
 * 
 * @author Felipe S. Lopes (felipe.lopes@sankhya.com.br)
 * @since 2022-09-22
 * @version 1.0.0
 * 
 */
public class Rentabilidade implements EventoProgramavelJava {

	@Override
	public void afterInsert(PersistenceEvent ctx) throws Exception {

		SessionHandle hnd = null;
		JdbcWrapper jdbc = null;

		// try {
		hnd = JapeSession.open();
		EntityFacade dwfEntityFacade = EntityFacadeFactory.getDWFFacade();
		jdbc = dwfEntityFacade.getJdbcWrapper();

		Nota nota = new Nota();
		nota.buildNewNota(ctx, jdbc);

		if (nota.getTipolancamento().equals("O")) {

			BigDecimal nunotaTemplate = null;
			NativeSql sql = new NativeSql(jdbc);
			sql.appendSql("SELECT NUNOTA ");
			sql.appendSql("FROM TGFCAB ");
			sql.appendSql("WHERE CODTIPOPER = :CODTIPOPER AND ROWNUM = 1 ");
			sql.appendSql("ORDER BY DTNEG DESC");
			sql.setNamedParameter("CODTIPOPER", nota.getCodtipoper());

			ResultSet result = sql.executeQuery();

			if (result.next())
				nunotaTemplate = result.getBigDecimal("NUNOTA");

			result.close();

			/*
			 * if (tipolancamento.equals("O")) throw new Exception("Teste: " +
			 * nunotaTemplate);
			 */
			createNewOrder(nota, nunotaTemplate);

		}
		JapeSession.close(hnd);
		/*
		 * } catch (Exception e) { e.printStackTrace(); e.getMessage();
		 * //MGEModelException.throwMe(e); } finally { JapeSession.close(hnd); }
		 */

	}

	@Override
	public void afterUpdate(PersistenceEvent ctx) throws Exception {

		SessionHandle hnd = null;
		JdbcWrapper jdbc = null;

		// try {
		hnd = JapeSession.open();
		EntityFacade dwfEntityFacade = EntityFacadeFactory.getDWFFacade();
		jdbc = dwfEntityFacade.getJdbcWrapper();

		Nota orcamento = new Nota();
		orcamento.buildNota(ctx, jdbc);

		updateProperty(orcamento);

		/*
		 * } catch (Exception e) { e.printStackTrace(); e.getMessage();
		 * //MGEModelException.throwMe(e); } finally { JapeSession.close(hnd); }
		 */
		JapeSession.close(hnd);

	}

	/**
	 * Este método cria uma nova nota na tabela de notas (TGFCAB) e retorna a
	 * instância do registro da nota recém criada.
	 * 
	 * O método cria objetos de acesso ao banco de dados para buscar as instâncias
	 * de um registro de uma nota modelo e, a partir dessa nota, e de um registro de
	 * tipo de operação, o qual será usado na nota que será inserida.
	 * 
	 * O método verificará a validade do número único da nota modelo passado para
	 * que não haja eros inesperados.
	 * 
	 * Em seguida, o método vai criar uma nova instância de registro da nota com
	 * base na nota do número único passado no parâmetro.
	 * 
	 * Por fim, o método invocará o método duplicate que irá duplicar essa instância
	 * de registro.
	 * 
	 * @param orcamento      instância de uma nota com as propriedades que serão
	 *                       usadas para criar uma nova instância de registro da
	 *                       nota.
	 * @param nunotaTemplate Número único da nota modelo.
	 * @return DynamicVO instância do registro da nota recém criada.
	 * @throws Exception
	 */
	public static DynamicVO createNewOrder(Nota orcamento, BigDecimal nunotaTemplate) throws Exception {

		JapeWrapper cabDAO = JapeFactory.dao(DynamicEntityNames.CABECALHO_NOTA);
		final JapeWrapper tipoOperacaoDAO = JapeFactory.dao(DynamicEntityNames.TIPO_OPERACAO);

		if (nunotaTemplate == null)
			throw new Exception("Nota modelo não existe ou não foi cadastrada corretamente."
					+ " Crie um modelo de nota na tela Modelo de " + "Notas de Pedidos com o mesmo Tipo de Operação");

		final DynamicVO cabTemplate = cabDAO.findByPK(nunotaTemplate);

		if (cabTemplate == null)
			throw new Exception("Nota modelo não existe ou não foi cadastrada corretamente."
					+ " Crie um modelo de nota na tela Modelo de " + "Notas de Pedidos com o mesmo Tipo de Operação");

		final DynamicVO topDoModelo = tipoOperacaoDAO.findByPK(cabTemplate.asBigDecimal("CODTIPOPER"),
				cabTemplate.asTimestamp("DHTIPOPER"));

		cabTemplate.setProperty("NUNOTA", null);
		cabTemplate.setProperty("DHTIPOPER", null);
		cabTemplate.setProperty("DHTIPVENDA", null);
		cabTemplate.setProperty("NUMNOTA", new BigDecimal("0"));
		cabTemplate.setProperty("DTNEG", TimeUtils.getNow());
		cabTemplate.setProperty("DTENTSAI", TimeUtils.getNow());
		cabTemplate.setProperty("CODEMP", orcamento.getCodemp());
		cabTemplate.setProperty("CODPARC", orcamento.getCodparc());
		cabTemplate.setProperty("OBSERVACAO", orcamento.getObservacao());
		cabTemplate.setProperty("CODTIPVENDA", orcamento.getCodtipvenda());
		cabTemplate.setProperty("CODUSU", orcamento.getCodusu());
		cabTemplate.setProperty("CODVEND", orcamento.getCodvend());
		cabTemplate.setProperty("VLRNOTA", orcamento.getVlrnota());
		cabTemplate.setProperty("VLRDESCTOT", orcamento.getDesctot());
		cabTemplate.setProperty("AD_CODOS", orcamento.getCodos());
		cabTemplate.setProperty("TIPMOV", topDoModelo.asString("TIPMOV"));
		cabTemplate.setProperty("CIF_FOB", "S");

		// duplica e cria a nova nunota
		DynamicVO novaNota = duplicate(cabDAO, cabTemplate);

		return novaNota;
	}

	/**
	 * Método que irá duplicar uma instância de registro de nota.
	 * 
	 * @param dao      Objeto de acesso ao banco de dados.
	 * @param modeloVO Instância do registro da nota modelo.
	 * @return instânci de registro de nota duplicada.
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public static DynamicVO duplicate(JapeWrapper dao, DynamicVO modeloVO) throws Exception {
		FluidCreateVO fluidCreateVO = dao.create();
		Iterator<VOProperty> iterator = modeloVO.iterator();

		while (iterator.hasNext()) {
			VOProperty property = iterator.next();
			fluidCreateVO.set(property.getName(), property.getValue());
		}

		return fluidCreateVO.save();
	}

	/**
	 * Método que atualiza um nota.
	 * 
	 * @param nota instância de uma nota com as propriedades que serão usadas para
	 *             atualizar uma nota.
	 * @throws Exception
	 */
	private void updateProperty(Nota nota) throws Exception {
		EntityFacade entityFacade = EntityFacadeFactory.getDWFFacade();
		EntityDAO dao = entityFacade.getDAOInstance(DynamicEntityNames.CABECALHO_NOTA);
		AuthenticationInfo authInfo = AuthenticationInfo.getCurrent();
		CACHelper cacHelper = new CACHelper();

		JapeWrapper cabDAO = JapeFactory.dao(DynamicEntityNames.CABECALHO_NOTA);
		DynamicVO cabVO = cabDAO.findByPK(nota.getNunota());

		PersistentLocalEntity entity = entityFacade.findEntityByPrimaryKey(dao.getEntityName(),
				cabVO.asBigDecimal("NUNOTA"));
		DynamicVO oldVO = (DynamicVO) entity.getValueObject();
		DynamicVO newVO = oldVO.buildClone();

		newVO.setProperty("CODEMP", nota.getCodemp());
		newVO.setProperty("CODPARC", nota.getCodparc());
		newVO.setProperty("OBSERVACAO", nota.getObservacao());
		newVO.setProperty("CODTIPVENDA", nota.getCodtipvenda());
		newVO.setProperty("CODUSU", nota.getCodusu());
		newVO.setProperty("CODVEND", nota.getCodvend());
		newVO.setProperty("VLRNOTA", nota.getVlrnota());
		newVO.setProperty("VLRDESCTOT", nota.getDesctot());
		newVO.setProperty("AD_CODOS", nota.getCodos());
		newVO.setProperty("CODTIPOPER", nota.getCodtipoper());

		PrePersistEntityState cabState = PrePersistEntityState.build(entityFacade, DynamicEntityNames.CABECALHO_NOTA,
				newVO, oldVO, entity);

		JapeSessionContext.putProperty(ListenerParameters.CENTRAIS, Boolean.TRUE);

		cacHelper.incluirAlterarCabecalho(authInfo, cabState);
	}

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
