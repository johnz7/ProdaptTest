package com.example.demo;

import com.example.demo.db.Book;
import com.example.demo.db.BookRepository;
import com.example.demo.google.GoogleBook;
import com.example.demo.google.GoogleBookService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


import java.util.List;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class BookControllerTests {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private WebApplicationContext context;
    @Autowired
    private BookRepository bookRepository;
    @MockitoBean
    private GoogleBookService googleBookService;

    @BeforeEach
    void setup() {
        bookRepository.deleteAll();
        bookRepository.save(new Book("lRtdEAAAQBAJ", "Spring in Action", "Craig Walls"));
        bookRepository.save(new Book("12muzgEACAAJ", "Effective Java", "Joshua Bloch"));
    }

    @Test
    void testGetAllBooks() throws Exception {
        mockMvc.perform(get("/books"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].title").value("Spring in Action"))
            .andExpect(jsonPath("$[1].title").value("Effective Java"));
    }
    
    @Test
    void testAddBookFromGoogle_success() throws Exception {

        GoogleBook.VolumeInfo volumeInfo =
                new GoogleBook.VolumeInfo(
                        "Clean Code",
                        List.of("Robert C. Martin"),
                        "2008",
                        "Prentice Hall",
                        464,
                        null,
                        null,
                        null,
                        null,
                        "en",
                        null
                );

        GoogleBook.Item item =
                new GoogleBook.Item(
                        "clean123",
                        "http://test",
                        volumeInfo,
                        null
                );

        Book book = new Book("clean123", "Clean Code", "Robert C. Martin");
        book.setPageCount(464);
        
        Mockito.when(googleBookService.addBookFromGoogle("clean123"))
        .thenReturn(book);

        mockMvc.perform(post("/books/clean123"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Clean Code"))
                .andExpect(jsonPath("$.author").value("Robert C. Martin"))
                .andExpect(jsonPath("$.pageCount").value(464));
    }

    @Test
    void testAddBook_invalid() throws Exception {

        Mockito.when(googleBookService.getBookById("invalid"))
                .thenReturn(null);

        mockMvc.perform(post("/books/invalid"))
                .andExpect(status().isBadRequest());
    }
}
