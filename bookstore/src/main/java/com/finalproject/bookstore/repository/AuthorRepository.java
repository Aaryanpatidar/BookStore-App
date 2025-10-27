package com.finalproject.bookstore.repository;

import com.finalproject.bookstore.model.Author;
import org.springframework.data.jpa.repository.JpaRepository;


public interface AuthorRepository extends JpaRepository<Author, Long> {
}
