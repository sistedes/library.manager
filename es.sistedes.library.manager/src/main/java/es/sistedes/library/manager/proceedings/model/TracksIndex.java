package es.sistedes.library.manager.proceedings.model;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections4.keyvalue.UnmodifiableMapEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;

/**
 * Class to save all {@link Track}s information in a single file as an
 * {@link AbstractProceedingsElement}
 * 
 * @author agomez
 *
 */
public class TracksIndex extends AbstractProceedingsElement implements Map<Integer, Track> {
	
	private static final Logger logger = LoggerFactory.getLogger(TracksIndex.class);
	
	@JsonIgnore
	protected File file;

	private List<Track> tracks = new ArrayList<>();

	private TracksIndex() {
	}
	
	private TracksIndex(File file, Collection<Track> tracks) {
		this.file = file;
		this.tracks.addAll(tracks);
		this.tracks.forEach(t -> t.setIndex(this));
	}

	/**
	 * Static method factory
	 * 
	 * @param tracks
	 * @return
	 */
	public static TracksIndex create(File file, Collection<Track> tracks) {
		return new TracksIndex(file, tracks);
	}

	/**
	 * @return the tracks
	 */
	public List<Track> getTracks() {
		return Collections.unmodifiableList(tracks);
	}
	
	@Override
	public int size() {
		return tracks.size();
	}

	@Override
	public boolean isEmpty() {
		return tracks.isEmpty();
	}

	@Override
	public boolean containsKey(Object key) {
		return tracks.stream().anyMatch(t -> key != null ? key.equals(t.getId()) : false);
	}

	@Override
	public boolean containsValue(Object value) {
		return tracks.stream().anyMatch(t -> value != null ? value.equals(t) : false);
	}

	@Override
	public Track get(Object key) {
		return tracks.stream().filter(t -> key != null ? key.equals(t.getId()) : false).findAny().get();
	}

	@Override
	public Track put(Integer key, Track value) {
		if (key == null) {
			throw new IllegalArgumentException("'TracksIndex' does not support null keys");
		}
		if (!key.equals(value.getId())) {
			throw new IllegalArgumentException("'key' must match 'track.getId() in 'TracksIndex'");
		}
		Track returnValue = null;
		if (this.containsKey(key)) {
			returnValue = this.get(key);
		}
		this.remove(key);
		tracks.add(value);
		tracks.sort((v1, v2) -> Integer.compare(v1.getId(), v2.getId()));
		return returnValue;
	}

	@Override
	public Track remove(Object key) {
		Optional<Track> returnValue = tracks.stream().filter(t -> key != null && key.equals(t.getId())).findFirst();
		returnValue.ifPresent(t -> tracks.remove(t));
		return returnValue.get();
	}

	@Override
	public void putAll(Map<? extends Integer, ? extends Track> m) {
		m.forEach((key, value) -> this.put(key, value));
	}

	@Override
	public void clear() {
		tracks.clear();
	}

	@Override
	public Set<Integer> keySet() {
		return tracks.stream().map(t -> t.getId()).collect(Collectors.toSet());
	}

	@Override
	public Collection<Track> values() {
		return Collections.unmodifiableCollection(tracks);
	}

	@Override
	public Set<Entry<Integer, Track>> entrySet() {
		Set<Entry<Integer, Track>> resultValue = new HashSet<>();
		tracks.forEach(t -> resultValue.add(new UnmodifiableMapEntry<Integer, Track>(t.getId(), t)));
		return resultValue;
	}
	
	public static TracksIndex load(File tracksFile) throws StreamReadException, DatabindException, IOException {
		JsonMapper mapper = JsonMapper.builder().build();
		mapper.setSerializationInclusion(Include.NON_NULL);
		mapper.setSerializationInclusion(Include.NON_EMPTY);
		mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
		mapper.configure(SerializationFeature.CLOSE_CLOSEABLE, true);
		return TracksIndex.create(tracksFile, mapper.readValue(tracksFile, new TypeReference<List<Track>>(){}));
	}
	
	public void save() {
		JsonMapper mapper = JsonMapper.builder().build();
		mapper.setSerializationInclusion(Include.NON_NULL);
		mapper.setSerializationInclusion(Include.NON_EMPTY);
		mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
		mapper.configure(SerializationFeature.CLOSE_CLOSEABLE, true);
		try {
			mapper.writeValue(file, this.tracks);
		} catch (IOException e) {
			logger.error(MessageFormat.format("Unable to write ''{0}''! ({1})", file, e.getLocalizedMessage()));
		}
	}
}