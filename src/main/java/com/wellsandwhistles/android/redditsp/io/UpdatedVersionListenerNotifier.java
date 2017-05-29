package com.wellsandwhistles.android.redditsp.io;

/** This file was either copied or modified from https://github.com/QuantumBadger/RedReader
 * under the Free Software Foundation General Public License version 3*/

import com.wellsandwhistles.android.redditsp.common.collections.WeakReferenceListManager;

public class UpdatedVersionListenerNotifier<K, V extends WritableObject<K>>
		implements WeakReferenceListManager.ArgOperator<UpdatedVersionListener<K, V>, V> {

	public void operate(UpdatedVersionListener<K, V> listener, V data) {
		listener.onUpdatedVersion(data);
	}
}
