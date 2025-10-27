package com.finalproject.bookstore.repository;

import com.finalproject.bookstore.model.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


public interface BookRepository extends JpaRepository<Book, Long> {
    // Simple filtering by title
    Page<Book> findByTitleContainingIgnoreCase(String title, Pageable pageable);


    // Find by author id
    @Query("select b from Book b where (:authorId is null or b.author.id = :authorId) and (:title is null or lower(b.title) like lower(concat('%', :title, '%')))" )
    Page<Book> findByAuthorAndTitle(@Param("authorId") Long authorId, @Param("title") String title, Pageable pageable);
}
