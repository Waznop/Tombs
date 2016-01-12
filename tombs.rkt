#lang racket

#|

TOMBS - Racket Version

Basic game engine, only provides turn actions

 1 | 2 | 3
-----------
 4 | 5 | 6
-----------
 7 | 8 | 9

Tile: '(Position, Content, Ownership)

Position: 1-9

Content:
0 Empty - 1 Tomb - 2 Thorn - 3 Ombra - 4 Magus - 5 Blade

Ownership
0 None - 1 Player1 - 2 Player2

|#

(define clear '((1 0 0) (2 0 0) (3 0 0) (4 0 0) (5 0 0) (6 0 0) (7 0 0) (8 0 0) (9 0 0)))
(define board '((1 0 0) (2 0 0) (3 0 0) (4 0 0) (5 0 0) (6 0 0) (7 0 0) (8 0 0) (9 0 0)))
(define preboard '((1 0 0) (2 0 0) (3 0 0) (4 0 0) (5 0 0) (6 0 0) (7 0 0) (8 0 0) (9 0 0)))
(define score1 0)
(define score2 0)
(define score3 0)
(define score4 0)

; board tile -> list-of-tiles
; returns tiles in the threat zones of a Blade tile

(define (btile b t)
  (match (first t)
    [1 `(,(second b) ,(fourth b))]
    [2 `(,(first b) ,(third b) ,(fifth b))]
    [3 `(,(second b) ,(sixth b))]
    [4 `(,(first b) ,(fifth b) ,(seventh b))]
    [5 `(,(second b) ,(fourth b) ,(sixth b) ,(eighth b))]
    [6 `(,(third b) ,(fifth b) ,(ninth b))]
    [7 `(,(fourth b) ,(eighth b))]
    [8 `(,(fifth b) ,(seventh b) ,(ninth b))]
    [9 `(,(sixth b) ,(eighth b))]))

; board tile -> list-of-tiles
; returns tiles in the threat zones of a Magus tile

(define (mtile b t)
  (match (first t)
    [1 `(,(fifth b) ,(ninth b))]
    [2 `(,(fourth b) ,(sixth b))]
    [3 `(,(fifth b) ,(seventh b))]
    [4 `(,(second b) ,(eighth b))]
    [5 `(,(first b) ,(third b) ,(seventh b) ,(ninth b))]
    [6 `(,(second b) ,(eighth b))]
    [7 `(,(third b) ,(fifth b))]
    [8 `(,(fourth b) ,(sixth b))]
    [9 `(,(first b) ,(fifth b))]))

; board tile -> list-of-tiles
; returns tiles in the threat zones of a Thorn tile

(define (ttile b t)
  (match (first t)
    [1 `(,(second b) ,(fourth b) ,(fifth b))]
    [2 `(,(first b) ,(third b) ,(fourth b) ,(fifth b) ,(sixth b))]
    [3 `(,(second b) ,(fifth b) ,(sixth b))]
    [4 `(,(first b) ,(second b) ,(fifth b) ,(seventh b) ,(eighth b))]
    [5 `(,(first b) ,(second b) ,(third b) ,(fourth b) ,(sixth b) ,(seventh b) ,(eighth b) ,(ninth b))]
    [6 `(,(second b) ,(third b) ,(fifth b) ,(eighth b) ,(ninth b))]
    [7 `(,(fourth b) ,(fifth b) ,(eighth b))]
    [8 `(,(fourth b) ,(fifth b) ,(sixth b) ,(seventh b) ,(ninth b))]
    [9 `(,(fifth b) ,(sixth b) ,(eighth b))]))

; board tile -> board
; returns board updated with the input tile

(define (update b t)
  (define (updateh b t i)
    (cond [(= i 1) (cons t (rest b))]
          [else (cons (first b) (updateh (rest b) t (- i 1)))]))
  (updateh b t (first t)))

; board list-of-tiles -> board
; returns board updated with tiles from the input list

(define (updateb b lst)
  (cond [(empty? lst) b]
        [else (updateb (update b (first lst)) (rest lst))]))

; index board -> tile
; returns the tile that matches the index on the board

(define (convert i b)
  (match i
    [1 (first b)] [2 (second b)] [3 (third b)] [4 (fourth b)] [5 (fifth b)]
    [6 (sixth b)] [7 (seventh b)] [8 (eighth b)] [9 (ninth b)]))

; tile list-of-tiles unit -> boolean
; returns whether there is a specific enemy unit to the given tile in the list of tiles
; ex: (enemy? '(5 4 2) '((4 5 1) (2 3 2)) 5) => #t

(define (enemy? t lst u)
  (cond [(empty? lst) false]
        [(and (= (second (first lst)) u) (not (= (third (first lst)) (third t)))) true]
        [else (enemy? t (rest lst) u)]))

; tile list-of-tiles -> boolean
; returns whether there is an enemy to the given tile in the list of tiles

(define (target? t lst)
  (cond [(empty? lst) false]
        [(not (or (= (third (first lst)) (third t)) (= (third (first lst)) 0))) true]
        [else (target? t (rest lst))]))

; tile1 tile2 -> boolean
; returns whether tile2 is an enemy to tile1

(define (singletarget? t1 t2)
  (cond [(not (or (= (third t2) (third t1)) (= (third t2) 0))) true]
        [else false]))

; board tile -> boolean
; checks if the tile can be placed* on the board
; ex: (check1 clear '(5 2 2)) => #t
; ex: (check1 (update clear '(5 1 0)) '(5 2 2)) => #f
; ex: (check1 (updateb clear '((5 1 0) (6 3 1))) '(5 2 2)) => #t
; ex: (check1 (updateb clear '((5 1 0) (6 3 1))) '(5 2 1)) => #f

(define (check1 b t)
  (cond [(and (= (second t) 2) (= (second (convert (first t) b)) 1)) ; thorn on tombstone
         (if (and (not (enemy? t (ttile b t) 2)) (target? t (ttile b t)))
             true false)] ; no adjacent enemy thorn but there's a target to kill
        [(and (= (second t) 3) (target? t `(,(convert (first t) b)))
              (not (= (second (convert (first t) b)) 3))) true] ; ombra on target
        [else (if (= (second (convert (first t) b)) 0) ; if it's an empty space
                  true false)]))

; board tile index -> board
; returns board after removing Thorn's target at i

(define (thornkill b t i)
  (define (thornkillh b t lst i)
    (cond [(empty? lst) (error 'invalid "invalid move")]
          [(and (= (first (first lst)) i)
                (not (or (= (third (first lst)) (third t)) (= (third (first lst)) 0))))
           (updateb b `((,i 0 0) ,t))]
          [else (thornkillh b t (rest lst) i)]))
  (thornkillh b t (ttile b t) i))

; board tile -> boolean
; checks if the tile would be threatened by something after it is placed on the board

(define (check2 b t)
  (cond [(enemy? t (btile b t) 5) false] ; blade zone
        [(enemy? t (mtile b t) 4) false] ; magus zone
        [else true]))

; tile list-of-tiles -> list-of-tiles
; checks for the tile's kills in the input list and returns a list of tombstone tiles
; ex: (maketomb '(5 5 2) '((2 4 1) (4 1 0) (6 2 3) (8 5 2))) => '((2 1 0) (6 1 0))

(define (maketomb t lst)
  (cond [(empty? lst) '()]
        [(singletarget? t (first lst)) (cons `(,(first (first lst)) 1 0) (maketomb t (rest lst)))]
        [else (maketomb t (rest lst))]))

; board tile -> board
; returns board after checking for tombstones created

(define (killupdate b t)
  (cond [(= (second t) 5) (updateb b (maketomb t (btile b t)))]
        [(= (second t) 4) (updateb b (maketomb t (mtile b t)))]
        [else b]))

; board tile index -> board
; returns board with the tile placed on it, and maybe call thornkill
; not activated thorn: i = 0

(define (place b t i)
  (cond [(and (= (second t) 2) (= (second (convert (first t) b)) 1)) ; thorn on tombstone
         (if (check1 b t) (thornkill b t i) (error 'noplace "cannot place here"))]
        [(check1 b t) (update b t)]
        [else (error 'noplace "cannot place here")]))

; board tile index -> board
; purely functional one-turn action

(define (movef b t i)
  (if (check2 (place b t i) t) (killupdate (place b t i) t) (error 'threatened "zone is threatened")))

; tile index -> updated-scores updated-board
; non-functional version of movef, changes the board

(define (move t i)
  (match t
    [`(,p 5 1) (set! score1 (+ score1 (length (maketomb t (btile board t)))))]
    [`(,p 5 2) (set! score2 (+ score2 (length (maketomb t (btile board t)))))]
    [`(,p 5 3) (set! score3 (+ score3 (length (maketomb t (btile board t)))))]
    [`(,p 5 4) (set! score4 (+ score4 (length (maketomb t (btile board t)))))]
    [`(,p 4 1) (set! score1 (+ score1 (length (maketomb t (mtile board t)))))]
    [`(,p 4 2) (set! score2 (+ score2 (length (maketomb t (mtile board t)))))]
    [`(,p 4 3) (set! score3 (+ score3 (length (maketomb t (mtile board t)))))]
    [`(,p 4 4) (set! score4 (+ score4 (length (maketomb t (mtile board t)))))]
    [x (void)])
  (set! preboard board)
  (set! board (movef board t i))
  (display board))

; undo 0 -> undo 1 move

(define (undo x)
  (set! board preboard))
