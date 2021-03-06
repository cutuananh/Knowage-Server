/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.
 *
 * Knowage is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Knowage is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package it.eng.spagobi.api.v2;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.LogMF;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import it.eng.spagobi.api.AbstractSpagoBIResource;
import it.eng.spagobi.commons.bo.Domain;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.dao.IDomainDAO;
import it.eng.spagobi.engines.config.bo.Engine;
import it.eng.spagobi.engines.config.bo.Exporters;
import it.eng.spagobi.engines.config.dao.IEngineDAO;
import it.eng.spagobi.services.rest.annotations.ManageAuthorization;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;

/**
 * @author Antonella Giachino (antonella.giachino@eng.it)
 */

@Path("/2.0/exporters")
@ManageAuthorization
public class ExporterResource extends AbstractSpagoBIResource {

	static private Logger logger = Logger.getLogger(ExporterResource.class);

	@GET
	@Path("/{label}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getExporters(@PathParam("label") String label) {

		logger.debug("IN: input label = " + label);

		List<Exporters> exportersLst = new ArrayList<Exporters>();
		JSONObject toReturn = new JSONObject();

		try {
			IEngineDAO engineDAO = DAOFactory.getEngineDAO();
			engineDAO.setUserProfile(getUserProfile());
			Engine engine = engineDAO.loadEngineByLabel(label);

			exportersLst = engineDAO.getAssociatedExporters(engine);
			String engineType = getDomainValue(engine.getBiobjTypeId());
			String driverName = engine.getDriverName();
			String exporterType = null;

			JSONArray exportersJSON = new JSONArray();
			for (int i = 0; i < exportersLst.size(); i++) {
				Exporters exporter = exportersLst.get(i);
				exporterType = getDomainValue(exporter.getDomainId());

				// create the json object with export configurations:
				JSONObject jsonExporterDett = new JSONObject();
				jsonExporterDett.put("name", exporterType);
				jsonExporterDett.put("engineType", engineType);
				jsonExporterDett.put("engineDriver", driverName);

				exportersJSON.put(jsonExporterDett);
			}
			toReturn.put("exporters", exportersJSON);
			logger.debug("Getting exporters for engine label=[" + label + "] - done successfully");

		} catch (Exception exception) {

			String messageToSend = String.format("Error while getting exporters for engine: [" + label + "]");
			logger.error(messageToSend, exception);
			throw new SpagoBIServiceException(messageToSend, exception);

		} finally {

			LogMF.debug(logger, "OUT: returning [{0}]", toReturn.toString());

		}

		return Response.ok(toReturn.toString()).build();
	}

	private String getDomainValue(Integer domainId) throws Exception {
		logger.debug("IN");

		String toReturn;
		IDomainDAO domainDAO = DAOFactory.getDomainDAO();
		Domain domainType = domainDAO.loadDomainById(domainId);
		toReturn = domainType.getValueCd();
		logger.debug("Getted domain value [" + toReturn + "] with id [" + domainId + "]");

		logger.debug("OUT");
		return toReturn;
	}

}