package com.mmk.mediaplayerbasic.data.local.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.mmk.mediaplayerbasic.data.local.entity.TrackEntity
import com.mmk.mediaplayerbasic.data.local.repository.TrackRepository
import dagger.hilt.android.scopes.ViewModelScoped
import javax.inject.Inject

@ViewModelScoped
class TrackPagingSource @Inject constructor(
    private val repository: TrackRepository
) : PagingSource<Int, TrackEntity>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, TrackEntity> {
        return try {
            val page = params.key ?: 0
            val tracks = repository.getPagedTracks(page, params.loadSize)

            LoadResult.Page(
                data = tracks,
                prevKey = if (page == 0) null else page - 1,
                nextKey = if (tracks.isEmpty()) null else page + 1
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, TrackEntity>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }
}