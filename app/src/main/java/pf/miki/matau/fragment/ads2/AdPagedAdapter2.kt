/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package pf.miki.matau.fragment.ads2

import android.arch.paging.PagedList
import android.arch.paging.PagedListAdapter
import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.ViewGroup
import pf.miki.matau.helpers.AdInteractionListener
import pf.miki.matau.R
import pf.miki.matau.repository.PAd

/**
 * A simple adapter implementation that shows Reddit posts.
 */

class AdPagedAdapter2(private val retryCallback: () -> Unit)

    : PagedListAdapter<PAd, RecyclerView.ViewHolder>(POST_COMPARATOR) {

    var listener: AdInteractionListener? = null

    private var networkState: NetworkState? = null
    val id2pos: MutableMap<String, Int> = mutableMapOf()

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (getItemViewType(position)) {
            R.layout.activity_ad_detail -> {
                val ad = getItem(position)
                ad?.let { a ->
                    id2pos[a.id] = position
//                    Log.i("Adapter", "$position <- ${a.id}")
                }
                (holder as AdCardViewHolder).bind(ad)
            }
            R.layout.network_state_item -> (holder as NetworkStateItemViewHolder).bindTo(networkState)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            R.layout.activity_ad_detail -> AdCardViewHolder.create(parent, listener)
            R.layout.network_state_item -> NetworkStateItemViewHolder.create(parent, retryCallback)
            else -> throw IllegalArgumentException("unknown view type $viewType")
        }
    }

    private fun hasExtraRow() = networkState != null && networkState != NetworkState.LOADED

    override fun getItemViewType(position: Int): Int {
        return if (position != itemCount - 1 || !hasExtraRow()) {
            R.layout.activity_ad_detail
        } else {
            R.layout.network_state_item
        }
    }

    override fun getItemCount(): Int {
        return super.getItemCount() + if (hasExtraRow()) 1 else 0
    }

    fun setNetworkState(newNetworkState: NetworkState?) {
        val previousState = this.networkState
        val hadExtraRow = hasExtraRow()
        this.networkState = newNetworkState
        val hasExtraRow = hasExtraRow()
        if (hadExtraRow != hasExtraRow) {
            if (hadExtraRow) {
                notifyItemRemoved(super.getItemCount())
            } else {
                notifyItemInserted(super.getItemCount())
            }
        } else if (hasExtraRow && previousState != newNetworkState) {
            notifyItemChanged(itemCount - 1)
        }
    }

    fun notifyChangedAds(list: List<PAd>?) {
        currentList?.let { modelList ->
            list?.forEach { ad ->
                // maybe the fragment as well as adapter has been rereated and id2pos is then empty and must be populated
                if (id2pos.isEmpty()) {
                    currentList?.mapIndexed { i, a -> a.id to i }?.toMap(id2pos)
//                    Log.i("Matau", "${id2pos.size}")
                }
                val pos = id2pos[ad.id]
//                Log.i("Adapter", "notify $pos <-- ${ad.id}")
                if (pos != null) {
                    if (modelList[pos] !== ad) { // different object ==> update attributes
//                        Log.i("Adapter", "       != objects")
                        modelList[pos]?.apply {
                            pinned = ad.pinned
                            lastViewed = ad.lastViewed
                            created = ad.created
                        }
                    }
                    notifyItemChanged(pos)
                }
            }
        }
    }

    override fun submitList(pagedList: PagedList<PAd>?) {
        super.submitList(pagedList)
        id2pos.clear()
    }

    companion object {
        val POST_COMPARATOR = object : DiffUtil.ItemCallback<PAd>() {
            override fun areContentsTheSame(oldItem: PAd, newItem: PAd): Boolean =
                    oldItem == newItem

            override fun areItemsTheSame(oldItem: PAd, newItem: PAd): Boolean =
                    oldItem.id == newItem.id

        }

    }


}
