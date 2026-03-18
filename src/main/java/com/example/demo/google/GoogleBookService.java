package com.example.demo.google;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.example.demo.db.Book;
import com.example.demo.db.BookRepository;

@Service
public class GoogleBookService {
    private final RestClient restClient;
    private final BookRepository bookRepository;


    public GoogleBookService(
            @Value("${google.books.base-url:https://www.googleapis.com/books/v1}") String baseUrl,
            BookRepository bookRepository) {

        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .build();
        this.bookRepository = bookRepository;
    }

    public GoogleBook searchBooks(String query, Integer maxResults, Integer startIndex) {
        return restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/volumes")
                        .queryParam("q", query)
                        .queryParam("maxResults", maxResults != null ? maxResults : 10)
                        .queryParam("startIndex", startIndex != null ? startIndex : 0)
                        .build())
                .retrieve()
                .body(GoogleBook.class);
    }

    public Book addBookFromGoogle(String googleId) {

        GoogleBook.Item item = getBookById(googleId);

        if (item == null || item.volumeInfo() == null) {
            return null;
        }

        GoogleBook.VolumeInfo info = item.volumeInfo();

        Book book = new Book();
        book.setId(item.id());
        book.setTitle(info.title());

        if (info.authors() != null && !info.authors().isEmpty()) {
            book.setAuthor(info.authors().get(0));
        }

        book.setPageCount(info.pageCount());

        return bookRepository.save(book);
    }
    
    public GoogleBook.Item getBookById(String googleId) {

        return restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/volumes/{id}")
                        .build(googleId))
                .retrieve()
                .body(GoogleBook.Item.class);
    }
	
}

