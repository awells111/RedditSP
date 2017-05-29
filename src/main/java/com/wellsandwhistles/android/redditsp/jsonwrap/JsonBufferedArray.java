package com.wellsandwhistles.android.redditsp.jsonwrap;

/** This file was either copied or modified from https://github.com/QuantumBadger/RedReader
 * under the Free Software Foundation General Public License version 3*/

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;


/**
 * A JSON array, which may be partially or fully received.
 */
public final class JsonBufferedArray extends JsonBuffered implements Iterable<JsonValue> {

	private final ArrayList<JsonValue> contents = new ArrayList<>(16);
	private int items = 0;

	@Override
	protected void buildBuffered(final JsonParser jp) throws IOException {

		JsonToken jt;

		while((jt = jp.nextToken()) != JsonToken.END_ARRAY) {

			final JsonValue value = new JsonValue(jp, jt);

			synchronized(this) {
				contents.add(value);
				items++;
				notifyAll();
			}

			value.buildInThisThread();
		}
	}

	/**
	 * This method will block until either: the array item is received, the
	 * array fails to parse, or the array is fully received and the index
	 * parameter is too large.
	 *
	 * @param id
	 *			The index into the array
	 * @return The value at position id in the array
	 * @throws InterruptedException
	 * @throws java.io.IOException
	 * @throws ArrayIndexOutOfBoundsException
	 */
	public JsonValue get(final int id) throws InterruptedException, IOException {

		if(id < 0)
			throw new ArrayIndexOutOfBoundsException(id);

		synchronized(this) {

			while(getStatus() == STATUS_LOADING && items <= id) {
				wait();
			}

			if(getStatus() != STATUS_FAILED || items > id) {
				return contents.get(id);
			}

			if(getStatus() == STATUS_FAILED) {
				throwFailReasonException();
			}

			throw new ArrayIndexOutOfBoundsException(id);
		}
	}

	/**
	 * This method will block until either: the array item is received, the
	 * array fails to parse, or the array is fully received and the index
	 * parameter is too large.
	 *
	 * @param id
	 *			The index into the array
	 * @return The value at position id in the array
	 * @throws InterruptedException
	 * @throws java.io.IOException
	 * @throws ArrayIndexOutOfBoundsException
	 */
	public String getString(final int id) throws InterruptedException, IOException {
		return get(id).asString();
	}

	/**
	 * This method will block until either: the array item is received, the
	 * array fails to parse, or the array is fully received and the index
	 * parameter is too large.
	 *
	 * @param id
	 *			The index into the array
	 * @return The value at position id in the array
	 * @throws InterruptedException
	 * @throws java.io.IOException
	 * @throws ArrayIndexOutOfBoundsException
	 */
	public Long getLong(final int id) throws InterruptedException, IOException {
		return get(id).asLong();
	}

	/**
	 * This method will block until either: the array item is received, the
	 * array fails to parse, or the array is fully received and the index
	 * parameter is too large.
	 *
	 * @param id
	 *			The index into the array
	 * @return The value at position id in the array
	 * @throws InterruptedException
	 * @throws java.io.IOException
	 * @throws ArrayIndexOutOfBoundsException
	 */
	public Double getDouble(final int id) throws InterruptedException, IOException {
		return get(id).asDouble();
	}

	/**
	 * This method will block until either: the array item is received, the
	 * array fails to parse, or the array is fully received and the index
	 * parameter is too large.
	 *
	 * @param id
	 *			The index into the array
	 * @return The value at position id in the array
	 * @throws InterruptedException
	 * @throws java.io.IOException
	 * @throws ArrayIndexOutOfBoundsException
	 */
	public Boolean getBoolean(final int id) throws InterruptedException, IOException {
		return get(id).asBoolean();
	}

	/**
	 * This method will block until either: the array item is received, the
	 * array fails to parse, or the array is fully received and the index
	 * parameter is too large.
	 *
	 * @param id
	 *			The index into the array
	 * @return The value at position id in the array
	 * @throws InterruptedException
	 * @throws java.io.IOException
	 * @throws ArrayIndexOutOfBoundsException
	 */
	public JsonBufferedObject getObject(final int id) throws InterruptedException, IOException {
		return get(id).asObject();
	}

	public <E> E getObject(final int id, final Class<E> clazz) throws InterruptedException, IOException, InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
		return get(id).asObject(clazz);
	}

	/**
	 * This method will block until either: the array item is received, the
	 * array fails to parse, or the array is fully received and the index
	 * parameter is too large.
	 *
	 * @param id
	 *			The index into the array
	 * @return The value at position id in the array
	 * @throws InterruptedException
	 * @throws java.io.IOException
	 * @throws ArrayIndexOutOfBoundsException
	 */
	public JsonBufferedArray getArray(final int id) throws InterruptedException, IOException {
		return get(id).asArray();
	}

	public Iterator<JsonValue> iterator() {
		return new JsonBufferedArrayIterator();
	}

	private class JsonBufferedArrayIterator implements Iterator<JsonValue> {

		private int currentId = 0;

		public boolean hasNext() {

			synchronized(JsonBufferedArray.this) {

				while(getStatus() == STATUS_LOADING && items <= currentId) {
					try {
						JsonBufferedArray.this.wait();
					} catch (final InterruptedException e) {
						throw new RuntimeException(e);
					}
				}

				if(getStatus() == STATUS_FAILED) {
					try {
						throwFailReasonException();
					} catch (final IOException e) {
						throw new RuntimeException(e);
					}
				}

				return items > currentId;
			}
		}

		public JsonValue next() {
			return contents.get(currentId++);
		}

		public void remove() {
			throw new UnsupportedOperationException();
		}
	}

	@Override
	protected void prettyPrint(final int indent, final StringBuilder sb) throws InterruptedException, IOException {

		if(join() != STATUS_LOADED) {
			throwFailReasonException();
		}

		sb.append('[');

		for(int item = 0; item < contents.size(); item++) {
			if(item != 0) sb.append(',');
			sb.append('\n');
			for(int i = 0; i < indent + 1; i++) sb.append("   ");
			contents.get(item).prettyPrint(indent + 1, sb);
		}

		sb.append('\n');
		for(int i = 0; i < indent; i++) sb.append("   ");
		sb.append(']');
	}

	public int getCurrentItemCount() {
		return items;
	}
}
