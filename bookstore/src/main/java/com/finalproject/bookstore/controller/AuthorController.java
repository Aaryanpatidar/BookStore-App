package com.finalproject.bookstore.controller;

import com.finalproject.bookstore.model.Author;
import com.finalproject.bookstore.repository.AuthorRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/authors")
public class AuthorController {

    private final AuthorRepository authorRepository;

    public AuthorController(AuthorRepository authorRepository) {
        this.authorRepository = authorRepository;
    }

    // -------------------------
    // GET: List all authors
    // -------------------------
    @Operation(
            summary = "Get list of authors",
            description = "Fetch all authors available in the database."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List of authors retrieved successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Author.class)))
    })
    @GetMapping
    public List<Author> all() {
        return authorRepository.findAll();
    }

    // -------------------------
    // GET: Get author by ID
    // -------------------------
    @Operation(
            summary = "Get author by ID",
            description = "Fetch details of a specific author using their ID."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Author found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Author.class))),
            @ApiResponse(responseCode = "404", description = "Author not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<Author> get(
            @Parameter(description = "ID of the author to retrieve") @PathVariable Long id) {
        return authorRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // -------------------------
    // POST: Create a new author
    // -------------------------
    @Operation(
            summary = "Create a new author",
            description = "Add a new author to the system."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Author created successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Author.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    @PostMapping
    public ResponseEntity<Author> create(@RequestBody Author author) {
        Author saved = authorRepository.save(author);
        return ResponseEntity.created(URI.create("/api/authors/" + saved.getId())).body(saved);
    }

    // -------------------------
    // PUT: Update existing author
    // -------------------------
    @Operation(
            summary = "Update existing author",
            description = "Modify the name or bio of an existing author."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Author updated successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Author.class))),
            @ApiResponse(responseCode = "404", description = "Author not found")
    })
    @PutMapping("/{id}")
    public ResponseEntity<Author> update(
            @Parameter(description = "ID of the author to update") @PathVariable Long id,
            @RequestBody Author author) {
        return authorRepository.findById(id)
                .map(existing -> {
                    existing.setName(author.getName());
                    existing.setBio(author.getBio());
                    authorRepository.save(existing);
                    return ResponseEntity.ok(existing);
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // -------------------------
    // DELETE: Remove author by ID
    // -------------------------
    @Operation(
            summary = "Delete author",
            description = "Remove an author and their associated books from the system."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Author deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Author not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @Parameter(description = "ID of the author to delete") @PathVariable Long id) {
        return authorRepository.findById(id)
                .map(author -> {
                    authorRepository.delete(author);
                    return ResponseEntity.noContent().<Void>build();
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
