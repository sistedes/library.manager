package es.sistedes.library.manager;

import es.sistedes.library.manager.proceedings.model.ConferenceData;

public interface IConferenceDataImporter {

	/**
	 * Returns the imported {@link ConferenceData}
	 * 
	 * @return
	 */
	public ConferenceData getData();

}