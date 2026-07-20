package com.nineleaps.BookExplorer.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "favourites")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Favourite {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "book_id", nullable = false)
    private String bookId; //
}
