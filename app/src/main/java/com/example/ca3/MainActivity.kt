package com.example.ca3

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ca3.ui.theme.Ca3Theme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class Movie(
    val title: String,
    val genre: String
)

class MovieRepository {
    private val _movies = MutableStateFlow<List<Movie>>(emptyList())
    val movies: StateFlow<List<Movie>> = _movies.asStateFlow()

    fun addMovie(movie: Movie) {
        _movies.value = _movies.value + movie
    }
}

class MovieViewModel : ViewModel() {
    private val repository = MovieRepository()

    val movieList: StateFlow<List<Movie>> = repository.movies

    val movieCount: StateFlow<Int> = repository.movies
        .map { it.size }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    fun addMovie(title: String, genre: String) {
        if (title.isNotBlank() && genre.isNotBlank()) {
            repository.addMovie(Movie(title, genre))
        }
    }
}

class MainActivity : ComponentActivity() {
    
    private val viewModel: MovieViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Ca3Theme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MovieWatchlistScreen(
                        viewModel = viewModel,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun MovieWatchlistScreen(viewModel: MovieViewModel, modifier: Modifier = Modifier) {
    val movies by viewModel.movieList.collectAsState()
    val count by viewModel.movieCount.collectAsState()

    var title by remember { mutableStateOf("") }
    var genre by remember { mutableStateOf("") }

    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        Text("Movie Watchlist", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Movie Title") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = genre,
            onValueChange = { genre = it },
            label = { Text("Genre") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                viewModel.addMovie(title, genre)
                title = ""
                genre = ""
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Add Movie")
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text("Total Movies: $count", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(movies) { movie ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = movie.title, 
                            style = MaterialTheme.typography.bodyLarge, 
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = movie.genre, 
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MovieWatchlistScreenPreview() {
    Ca3Theme {
        MovieWatchlistScreen(viewModel = MovieViewModel())
    }
}