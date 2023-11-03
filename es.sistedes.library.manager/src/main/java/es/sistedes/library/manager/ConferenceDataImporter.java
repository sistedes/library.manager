package es.sistedes.library.manager;

import es.sistedes.library.manager.proceedings.model.ConferenceData;

public interface ConferenceDataImporter {

	/**
	 * Returns the imported {@link ConferenceData}
	 * 
	 * @return
	 */
	ConferenceData getData();

}