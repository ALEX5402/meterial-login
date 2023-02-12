package bruhcollective.itaysonlab.microapp.library.ui.library_pages

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import bruhcollective.itaysonlab.jetisteam.HostSteamClient
import bruhcollective.itaysonlab.jetisteam.uikit.components.RoundedPage
import bruhcollective.itaysonlab.jetisteam.util.CdnUrlUtil
import bruhcollective.itaysonlab.ksteam.handlers.library
import bruhcollective.itaysonlab.ksteam.models.library.LibraryCollection
import coil.compose.AsyncImage
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@Composable
internal fun CollectionPage(
    collection: LibraryCollection,
    viewModel: CollectionPageViewModel = hiltViewModel()
) {
    val data = viewModel.getData(collection.id).collectAsStateWithLifecycle(initialValue = emptyList())

    RoundedPage(modifier = Modifier.fillMaxSize()) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            itemsIndexed(data.value) { index, game ->
                LibraryItem(remember(game.imageCapsuleFileName) {
                    CdnUrlUtil.buildAppUrl(game.id.toInt(), "library_600x900.jpg")
                }, modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .let {
                        when (index) {
                            0 -> it.clip(
                                MaterialTheme.shapes.large.copy(
                                    topEnd = CornerSize(0.dp),
                                    bottomStart = CornerSize(0.dp),
                                    bottomEnd = CornerSize(0.dp)
                                )
                            )

                            2 -> it.clip(
                                MaterialTheme.shapes.large.copy(
                                    topStart = CornerSize(0.dp),
                                    bottomStart = CornerSize(0.dp),
                                    bottomEnd = CornerSize(0.dp)
                                )
                            )

                            else -> it
                        }
                    }
                    .clickable {

                    })
            }
        }
    }
}

@HiltViewModel
internal class CollectionPageViewModel @Inject constructor(
    private val steamClient: HostSteamClient
): ViewModel() {
    fun getData(id: String) = steamClient.client.library.getAppsInCollection(id)
}

@Composable
internal fun LibraryItem(
    image: String,
    modifier: Modifier,
    placeholderColor: Color = MaterialTheme.colorScheme.surface
) {
    AsyncImage(
        model = image,
        contentDescription = null,
        modifier = modifier,
        contentScale = ContentScale.FillBounds,
        placeholder = ColorPainter(placeholderColor),
        error = ColorPainter(placeholderColor),
    )
}