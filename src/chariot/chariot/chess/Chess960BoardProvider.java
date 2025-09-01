package chariot.chess;

import module java.base;

public interface Chess960BoardProvider extends BoardProvider {

    default Board fromPosition(int position) {
        return fromFEN(positionToFEN(position));
    }

    default Board random960() {
        return fromPosition(new Random().nextInt(960));
    }

    default Board fromFEN(String fen) {
        return fromFEN("chess960", fen);
    }

    default String initialCastlingByPiecesSetup(String pieces) {
        return IntStream.range(0, 8)
            .filter(i -> pieces.charAt(i) == 'r')
            .mapToObj(i -> Character.toString('a' + i))
            .collect(Collectors.joining())
            .transform(s -> "%s%s".formatted(s.toUpperCase(Locale.ROOT), s));
    }

    default String positionToFEN(int position) {
        String pieces = switch(position) {
            case   0 -> "bbqnnrkr"; case   1 -> "bqnbnrkr"; case   2 -> "bqnnrbkr"; case   3 -> "bqnnrkrb"; case   4 -> "qbbnnrkr";
            case   5 -> "qnbbnrkr"; case   6 -> "qnbnrbkr"; case   7 -> "qnbnrkrb"; case   8 -> "qbnnbrkr"; case   9 -> "qnnbbrkr";
            case  10 -> "qnnrbbkr"; case  11 -> "qnnrbkrb"; case  12 -> "qbnnrkbr"; case  13 -> "qnnbrkbr"; case  14 -> "qnnrkbbr";
            case  15 -> "qnnrkrbb"; case  16 -> "bbnqnrkr"; case  17 -> "bnqbnrkr"; case  18 -> "bnqnrbkr"; case  19 -> "bnqnrkrb";
            case  20 -> "nbbqnrkr"; case  21 -> "nqbbnrkr"; case  22 -> "nqbnrbkr"; case  23 -> "nqbnrkrb"; case  24 -> "nbqnbrkr";
            case  25 -> "nqnbbrkr"; case  26 -> "nqnrbbkr"; case  27 -> "nqnrbkrb"; case  28 -> "nbqnrkbr"; case  29 -> "nqnbrkbr";
            case  30 -> "nqnrkbbr"; case  31 -> "nqnrkrbb"; case  32 -> "bbnnqrkr"; case  33 -> "bnnbqrkr"; case  34 -> "bnnqrbkr";
            case  35 -> "bnnqrkrb"; case  36 -> "nbbnqrkr"; case  37 -> "nnbbqrkr"; case  38 -> "nnbqrbkr"; case  39 -> "nnbqrkrb";
            case  40 -> "nbnqbrkr"; case  41 -> "nnqbbrkr"; case  42 -> "nnqrbbkr"; case  43 -> "nnqrbkrb"; case  44 -> "nbnqrkbr";
            case  45 -> "nnqbrkbr"; case  46 -> "nnqrkbbr"; case  47 -> "nnqrkrbb"; case  48 -> "bbnnrqkr"; case  49 -> "bnnbrqkr";
            case  50 -> "bnnrqbkr"; case  51 -> "bnnrqkrb"; case  52 -> "nbbnrqkr"; case  53 -> "nnbbrqkr"; case  54 -> "nnbrqbkr";
            case  55 -> "nnbrqkrb"; case  56 -> "nbnrbqkr"; case  57 -> "nnrbbqkr"; case  58 -> "nnrqbbkr"; case  59 -> "nnrqbkrb";
            case  60 -> "nbnrqkbr"; case  61 -> "nnrbqkbr"; case  62 -> "nnrqkbbr"; case  63 -> "nnrqkrbb"; case  64 -> "bbnnrkqr";
            case  65 -> "bnnbrkqr"; case  66 -> "bnnrkbqr"; case  67 -> "bnnrkqrb"; case  68 -> "nbbnrkqr"; case  69 -> "nnbbrkqr";
            case  70 -> "nnbrkbqr"; case  71 -> "nnbrkqrb"; case  72 -> "nbnrbkqr"; case  73 -> "nnrbbkqr"; case  74 -> "nnrkbbqr";
            case  75 -> "nnrkbqrb"; case  76 -> "nbnrkqbr"; case  77 -> "nnrbkqbr"; case  78 -> "nnrkqbbr"; case  79 -> "nnrkqrbb";
            case  80 -> "bbnnrkrq"; case  81 -> "bnnbrkrq"; case  82 -> "bnnrkbrq"; case  83 -> "bnnrkrqb"; case  84 -> "nbbnrkrq";
            case  85 -> "nnbbrkrq"; case  86 -> "nnbrkbrq"; case  87 -> "nnbrkrqb"; case  88 -> "nbnrbkrq"; case  89 -> "nnrbbkrq";
            case  90 -> "nnrkbbrq"; case  91 -> "nnrkbrqb"; case  92 -> "nbnrkrbq"; case  93 -> "nnrbkrbq"; case  94 -> "nnrkrbbq";
            case  95 -> "nnrkrqbb"; case  96 -> "bbqnrnkr"; case  97 -> "bqnbrnkr"; case  98 -> "bqnrnbkr"; case  99 -> "bqnrnkrb";
            case 100 -> "qbbnrnkr"; case 101 -> "qnbbrnkr"; case 102 -> "qnbrnbkr"; case 103 -> "qnbrnkrb"; case 104 -> "qbnrbnkr";
            case 105 -> "qnrbbnkr"; case 106 -> "qnrnbbkr"; case 107 -> "qnrnbkrb"; case 108 -> "qbnrnkbr"; case 109 -> "qnrbnkbr";
            case 110 -> "qnrnkbbr"; case 111 -> "qnrnkrbb"; case 112 -> "bbnqrnkr"; case 113 -> "bnqbrnkr"; case 114 -> "bnqrnbkr";
            case 115 -> "bnqrnkrb"; case 116 -> "nbbqrnkr"; case 117 -> "nqbbrnkr"; case 118 -> "nqbrnbkr"; case 119 -> "nqbrnkrb";
            case 120 -> "nbqrbnkr"; case 121 -> "nqrbbnkr"; case 122 -> "nqrnbbkr"; case 123 -> "nqrnbkrb"; case 124 -> "nbqrnkbr";
            case 125 -> "nqrbnkbr"; case 126 -> "nqrnkbbr"; case 127 -> "nqrnkrbb"; case 128 -> "bbnrqnkr"; case 129 -> "bnrbqnkr";
            case 130 -> "bnrqnbkr"; case 131 -> "bnrqnkrb"; case 132 -> "nbbrqnkr"; case 133 -> "nrbbqnkr"; case 134 -> "nrbqnbkr";
            case 135 -> "nrbqnkrb"; case 136 -> "nbrqbnkr"; case 137 -> "nrqbbnkr"; case 138 -> "nrqnbbkr"; case 139 -> "nrqnbkrb";
            case 140 -> "nbrqnkbr"; case 141 -> "nrqbnkbr"; case 142 -> "nrqnkbbr"; case 143 -> "nrqnkrbb"; case 144 -> "bbnrnqkr";
            case 145 -> "bnrbnqkr"; case 146 -> "bnrnqbkr"; case 147 -> "bnrnqkrb"; case 148 -> "nbbrnqkr"; case 149 -> "nrbbnqkr";
            case 150 -> "nrbnqbkr"; case 151 -> "nrbnqkrb"; case 152 -> "nbrnbqkr"; case 153 -> "nrnbbqkr"; case 154 -> "nrnqbbkr";
            case 155 -> "nrnqbkrb"; case 156 -> "nbrnqkbr"; case 157 -> "nrnbqkbr"; case 158 -> "nrnqkbbr"; case 159 -> "nrnqkrbb";
            case 160 -> "bbnrnkqr"; case 161 -> "bnrbnkqr"; case 162 -> "bnrnkbqr"; case 163 -> "bnrnkqrb"; case 164 -> "nbbrnkqr";
            case 165 -> "nrbbnkqr"; case 166 -> "nrbnkbqr"; case 167 -> "nrbnkqrb"; case 168 -> "nbrnbkqr"; case 169 -> "nrnbbkqr";
            case 170 -> "nrnkbbqr"; case 171 -> "nrnkbqrb"; case 172 -> "nbrnkqbr"; case 173 -> "nrnbkqbr"; case 174 -> "nrnkqbbr";
            case 175 -> "nrnkqrbb"; case 176 -> "bbnrnkrq"; case 177 -> "bnrbnkrq"; case 178 -> "bnrnkbrq"; case 179 -> "bnrnkrqb";
            case 180 -> "nbbrnkrq"; case 181 -> "nrbbnkrq"; case 182 -> "nrbnkbrq"; case 183 -> "nrbnkrqb"; case 184 -> "nbrnbkrq";
            case 185 -> "nrnbbkrq"; case 186 -> "nrnkbbrq"; case 187 -> "nrnkbrqb"; case 188 -> "nbrnkrbq"; case 189 -> "nrnbkrbq";
            case 190 -> "nrnkrbbq"; case 191 -> "nrnkrqbb"; case 192 -> "bbqnrknr"; case 193 -> "bqnbrknr"; case 194 -> "bqnrkbnr";
            case 195 -> "bqnrknrb"; case 196 -> "qbbnrknr"; case 197 -> "qnbbrknr"; case 198 -> "qnbrkbnr"; case 199 -> "qnbrknrb";
            case 200 -> "qbnrbknr"; case 201 -> "qnrbbknr"; case 202 -> "qnrkbbnr"; case 203 -> "qnrkbnrb"; case 204 -> "qbnrknbr";
            case 205 -> "qnrbknbr"; case 206 -> "qnrknbbr"; case 207 -> "qnrknrbb"; case 208 -> "bbnqrknr"; case 209 -> "bnqbrknr";
            case 210 -> "bnqrkbnr"; case 211 -> "bnqrknrb"; case 212 -> "nbbqrknr"; case 213 -> "nqbbrknr"; case 214 -> "nqbrkbnr";
            case 215 -> "nqbrknrb"; case 216 -> "nbqrbknr"; case 217 -> "nqrbbknr"; case 218 -> "nqrkbbnr"; case 219 -> "nqrkbnrb";
            case 220 -> "nbqrknbr"; case 221 -> "nqrbknbr"; case 222 -> "nqrknbbr"; case 223 -> "nqrknrbb"; case 224 -> "bbnrqknr";
            case 225 -> "bnrbqknr"; case 226 -> "bnrqkbnr"; case 227 -> "bnrqknrb"; case 228 -> "nbbrqknr"; case 229 -> "nrbbqknr";
            case 230 -> "nrbqkbnr"; case 231 -> "nrbqknrb"; case 232 -> "nbrqbknr"; case 233 -> "nrqbbknr"; case 234 -> "nrqkbbnr";
            case 235 -> "nrqkbnrb"; case 236 -> "nbrqknbr"; case 237 -> "nrqbknbr"; case 238 -> "nrqknbbr"; case 239 -> "nrqknrbb";
            case 240 -> "bbnrkqnr"; case 241 -> "bnrbkqnr"; case 242 -> "bnrkqbnr"; case 243 -> "bnrkqnrb"; case 244 -> "nbbrkqnr";
            case 245 -> "nrbbkqnr"; case 246 -> "nrbkqbnr"; case 247 -> "nrbkqnrb"; case 248 -> "nbrkbqnr"; case 249 -> "nrkbbqnr";
            case 250 -> "nrkqbbnr"; case 251 -> "nrkqbnrb"; case 252 -> "nbrkqnbr"; case 253 -> "nrkbqnbr"; case 254 -> "nrkqnbbr";
            case 255 -> "nrkqnrbb"; case 256 -> "bbnrknqr"; case 257 -> "bnrbknqr"; case 258 -> "bnrknbqr"; case 259 -> "bnrknqrb";
            case 260 -> "nbbrknqr"; case 261 -> "nrbbknqr"; case 262 -> "nrbknbqr"; case 263 -> "nrbknqrb"; case 264 -> "nbrkbnqr";
            case 265 -> "nrkbbnqr"; case 266 -> "nrknbbqr"; case 267 -> "nrknbqrb"; case 268 -> "nbrknqbr"; case 269 -> "nrkbnqbr";
            case 270 -> "nrknqbbr"; case 271 -> "nrknqrbb"; case 272 -> "bbnrknrq"; case 273 -> "bnrbknrq"; case 274 -> "bnrknbrq";
            case 275 -> "bnrknrqb"; case 276 -> "nbbrknrq"; case 277 -> "nrbbknrq"; case 278 -> "nrbknbrq"; case 279 -> "nrbknrqb";
            case 280 -> "nbrkbnrq"; case 281 -> "nrkbbnrq"; case 282 -> "nrknbbrq"; case 283 -> "nrknbrqb"; case 284 -> "nbrknrbq";
            case 285 -> "nrkbnrbq"; case 286 -> "nrknrbbq"; case 287 -> "nrknrqbb"; case 288 -> "bbqnrkrn"; case 289 -> "bqnbrkrn";
            case 290 -> "bqnrkbrn"; case 291 -> "bqnrkrnb"; case 292 -> "qbbnrkrn"; case 293 -> "qnbbrkrn"; case 294 -> "qnbrkbrn";
            case 295 -> "qnbrkrnb"; case 296 -> "qbnrbkrn"; case 297 -> "qnrbbkrn"; case 298 -> "qnrkbbrn"; case 299 -> "qnrkbrnb";
            case 300 -> "qbnrkrbn"; case 301 -> "qnrbkrbn"; case 302 -> "qnrkrbbn"; case 303 -> "qnrkrnbb"; case 304 -> "bbnqrkrn";
            case 305 -> "bnqbrkrn"; case 306 -> "bnqrkbrn"; case 307 -> "bnqrkrnb"; case 308 -> "nbbqrkrn"; case 309 -> "nqbbrkrn";
            case 310 -> "nqbrkbrn"; case 311 -> "nqbrkrnb"; case 312 -> "nbqrbkrn"; case 313 -> "nqrbbkrn"; case 314 -> "nqrkbbrn";
            case 315 -> "nqrkbrnb"; case 316 -> "nbqrkrbn"; case 317 -> "nqrbkrbn"; case 318 -> "nqrkrbbn"; case 319 -> "nqrkrnbb";
            case 320 -> "bbnrqkrn"; case 321 -> "bnrbqkrn"; case 322 -> "bnrqkbrn"; case 323 -> "bnrqkrnb"; case 324 -> "nbbrqkrn";
            case 325 -> "nrbbqkrn"; case 326 -> "nrbqkbrn"; case 327 -> "nrbqkrnb"; case 328 -> "nbrqbkrn"; case 329 -> "nrqbbkrn";
            case 330 -> "nrqkbbrn"; case 331 -> "nrqkbrnb"; case 332 -> "nbrqkrbn"; case 333 -> "nrqbkrbn"; case 334 -> "nrqkrbbn";
            case 335 -> "nrqkrnbb"; case 336 -> "bbnrkqrn"; case 337 -> "bnrbkqrn"; case 338 -> "bnrkqbrn"; case 339 -> "bnrkqrnb";
            case 340 -> "nbbrkqrn"; case 341 -> "nrbbkqrn"; case 342 -> "nrbkqbrn"; case 343 -> "nrbkqrnb"; case 344 -> "nbrkbqrn";
            case 345 -> "nrkbbqrn"; case 346 -> "nrkqbbrn"; case 347 -> "nrkqbrnb"; case 348 -> "nbrkqrbn"; case 349 -> "nrkbqrbn";
            case 350 -> "nrkqrbbn"; case 351 -> "nrkqrnbb"; case 352 -> "bbnrkrqn"; case 353 -> "bnrbkrqn"; case 354 -> "bnrkrbqn";
            case 355 -> "bnrkrqnb"; case 356 -> "nbbrkrqn"; case 357 -> "nrbbkrqn"; case 358 -> "nrbkrbqn"; case 359 -> "nrbkrqnb";
            case 360 -> "nbrkbrqn"; case 361 -> "nrkbbrqn"; case 362 -> "nrkrbbqn"; case 363 -> "nrkrbqnb"; case 364 -> "nbrkrqbn";
            case 365 -> "nrkbrqbn"; case 366 -> "nrkrqbbn"; case 367 -> "nrkrqnbb"; case 368 -> "bbnrkrnq"; case 369 -> "bnrbkrnq";
            case 370 -> "bnrkrbnq"; case 371 -> "bnrkrnqb"; case 372 -> "nbbrkrnq"; case 373 -> "nrbbkrnq"; case 374 -> "nrbkrbnq";
            case 375 -> "nrbkrnqb"; case 376 -> "nbrkbrnq"; case 377 -> "nrkbbrnq"; case 378 -> "nrkrbbnq"; case 379 -> "nrkrbnqb";
            case 380 -> "nbrkrnbq"; case 381 -> "nrkbrnbq"; case 382 -> "nrkrnbbq"; case 383 -> "nrkrnqbb"; case 384 -> "bbqrnnkr";
            case 385 -> "bqrbnnkr"; case 386 -> "bqrnnbkr"; case 387 -> "bqrnnkrb"; case 388 -> "qbbrnnkr"; case 389 -> "qrbbnnkr";
            case 390 -> "qrbnnbkr"; case 391 -> "qrbnnkrb"; case 392 -> "qbrnbnkr"; case 393 -> "qrnbbnkr"; case 394 -> "qrnnbbkr";
            case 395 -> "qrnnbkrb"; case 396 -> "qbrnnkbr"; case 397 -> "qrnbnkbr"; case 398 -> "qrnnkbbr"; case 399 -> "qrnnkrbb";
            case 400 -> "bbrqnnkr"; case 401 -> "brqbnnkr"; case 402 -> "brqnnbkr"; case 403 -> "brqnnkrb"; case 404 -> "rbbqnnkr";
            case 405 -> "rqbbnnkr"; case 406 -> "rqbnnbkr"; case 407 -> "rqbnnkrb"; case 408 -> "rbqnbnkr"; case 409 -> "rqnbbnkr";
            case 410 -> "rqnnbbkr"; case 411 -> "rqnnbkrb"; case 412 -> "rbqnnkbr"; case 413 -> "rqnbnkbr"; case 414 -> "rqnnkbbr";
            case 415 -> "rqnnkrbb"; case 416 -> "bbrnqnkr"; case 417 -> "brnbqnkr"; case 418 -> "brnqnbkr"; case 419 -> "brnqnkrb";
            case 420 -> "rbbnqnkr"; case 421 -> "rnbbqnkr"; case 422 -> "rnbqnbkr"; case 423 -> "rnbqnkrb"; case 424 -> "rbnqbnkr";
            case 425 -> "rnqbbnkr"; case 426 -> "rnqnbbkr"; case 427 -> "rnqnbkrb"; case 428 -> "rbnqnkbr"; case 429 -> "rnqbnkbr";
            case 430 -> "rnqnkbbr"; case 431 -> "rnqnkrbb"; case 432 -> "bbrnnqkr"; case 433 -> "brnbnqkr"; case 434 -> "brnnqbkr";
            case 435 -> "brnnqkrb"; case 436 -> "rbbnnqkr"; case 437 -> "rnbbnqkr"; case 438 -> "rnbnqbkr"; case 439 -> "rnbnqkrb";
            case 440 -> "rbnnbqkr"; case 441 -> "rnnbbqkr"; case 442 -> "rnnqbbkr"; case 443 -> "rnnqbkrb"; case 444 -> "rbnnqkbr";
            case 445 -> "rnnbqkbr"; case 446 -> "rnnqkbbr"; case 447 -> "rnnqkrbb"; case 448 -> "bbrnnkqr"; case 449 -> "brnbnkqr";
            case 450 -> "brnnkbqr"; case 451 -> "brnnkqrb"; case 452 -> "rbbnnkqr"; case 453 -> "rnbbnkqr"; case 454 -> "rnbnkbqr";
            case 455 -> "rnbnkqrb"; case 456 -> "rbnnbkqr"; case 457 -> "rnnbbkqr"; case 458 -> "rnnkbbqr"; case 459 -> "rnnkbqrb";
            case 460 -> "rbnnkqbr"; case 461 -> "rnnbkqbr"; case 462 -> "rnnkqbbr"; case 463 -> "rnnkqrbb"; case 464 -> "bbrnnkrq";
            case 465 -> "brnbnkrq"; case 466 -> "brnnkbrq"; case 467 -> "brnnkrqb"; case 468 -> "rbbnnkrq"; case 469 -> "rnbbnkrq";
            case 470 -> "rnbnkbrq"; case 471 -> "rnbnkrqb"; case 472 -> "rbnnbkrq"; case 473 -> "rnnbbkrq"; case 474 -> "rnnkbbrq";
            case 475 -> "rnnkbrqb"; case 476 -> "rbnnkrbq"; case 477 -> "rnnbkrbq"; case 478 -> "rnnkrbbq"; case 479 -> "rnnkrqbb";
            case 480 -> "bbqrnknr"; case 481 -> "bqrbnknr"; case 482 -> "bqrnkbnr"; case 483 -> "bqrnknrb"; case 484 -> "qbbrnknr";
            case 485 -> "qrbbnknr"; case 486 -> "qrbnkbnr"; case 487 -> "qrbnknrb"; case 488 -> "qbrnbknr"; case 489 -> "qrnbbknr";
            case 490 -> "qrnkbbnr"; case 491 -> "qrnkbnrb"; case 492 -> "qbrnknbr"; case 493 -> "qrnbknbr"; case 494 -> "qrnknbbr";
            case 495 -> "qrnknrbb"; case 496 -> "bbrqnknr"; case 497 -> "brqbnknr"; case 498 -> "brqnkbnr"; case 499 -> "brqnknrb";
            case 500 -> "rbbqnknr"; case 501 -> "rqbbnknr"; case 502 -> "rqbnkbnr"; case 503 -> "rqbnknrb"; case 504 -> "rbqnbknr";
            case 505 -> "rqnbbknr"; case 506 -> "rqnkbbnr"; case 507 -> "rqnkbnrb"; case 508 -> "rbqnknbr"; case 509 -> "rqnbknbr";
            case 510 -> "rqnknbbr"; case 511 -> "rqnknrbb"; case 512 -> "bbrnqknr"; case 513 -> "brnbqknr"; case 514 -> "brnqkbnr";
            case 515 -> "brnqknrb"; case 516 -> "rbbnqknr"; case 517 -> "rnbbqknr"; case 518 -> "rnbqkbnr"; case 519 -> "rnbqknrb";
            case 520 -> "rbnqbknr"; case 521 -> "rnqbbknr"; case 522 -> "rnqkbbnr"; case 523 -> "rnqkbnrb"; case 524 -> "rbnqknbr";
            case 525 -> "rnqbknbr"; case 526 -> "rnqknbbr"; case 527 -> "rnqknrbb"; case 528 -> "bbrnkqnr"; case 529 -> "brnbkqnr";
            case 530 -> "brnkqbnr"; case 531 -> "brnkqnrb"; case 532 -> "rbbnkqnr"; case 533 -> "rnbbkqnr"; case 534 -> "rnbkqbnr";
            case 535 -> "rnbkqnrb"; case 536 -> "rbnkbqnr"; case 537 -> "rnkbbqnr"; case 538 -> "rnkqbbnr"; case 539 -> "rnkqbnrb";
            case 540 -> "rbnkqnbr"; case 541 -> "rnkbqnbr"; case 542 -> "rnkqnbbr"; case 543 -> "rnkqnrbb"; case 544 -> "bbrnknqr";
            case 545 -> "brnbknqr"; case 546 -> "brnknbqr"; case 547 -> "brnknqrb"; case 548 -> "rbbnknqr"; case 549 -> "rnbbknqr";
            case 550 -> "rnbknbqr"; case 551 -> "rnbknqrb"; case 552 -> "rbnkbnqr"; case 553 -> "rnkbbnqr"; case 554 -> "rnknbbqr";
            case 555 -> "rnknbqrb"; case 556 -> "rbnknqbr"; case 557 -> "rnkbnqbr"; case 558 -> "rnknqbbr"; case 559 -> "rnknqrbb";
            case 560 -> "bbrnknrq"; case 561 -> "brnbknrq"; case 562 -> "brnknbrq"; case 563 -> "brnknrqb"; case 564 -> "rbbnknrq";
            case 565 -> "rnbbknrq"; case 566 -> "rnbknbrq"; case 567 -> "rnbknrqb"; case 568 -> "rbnkbnrq"; case 569 -> "rnkbbnrq";
            case 570 -> "rnknbbrq"; case 571 -> "rnknbrqb"; case 572 -> "rbnknrbq"; case 573 -> "rnkbnrbq"; case 574 -> "rnknrbbq";
            case 575 -> "rnknrqbb"; case 576 -> "bbqrnkrn"; case 577 -> "bqrbnkrn"; case 578 -> "bqrnkbrn"; case 579 -> "bqrnkrnb";
            case 580 -> "qbbrnkrn"; case 581 -> "qrbbnkrn"; case 582 -> "qrbnkbrn"; case 583 -> "qrbnkrnb"; case 584 -> "qbrnbkrn";
            case 585 -> "qrnbbkrn"; case 586 -> "qrnkbbrn"; case 587 -> "qrnkbrnb"; case 588 -> "qbrnkrbn"; case 589 -> "qrnbkrbn";
            case 590 -> "qrnkrbbn"; case 591 -> "qrnkrnbb"; case 592 -> "bbrqnkrn"; case 593 -> "brqbnkrn"; case 594 -> "brqnkbrn";
            case 595 -> "brqnkrnb"; case 596 -> "rbbqnkrn"; case 597 -> "rqbbnkrn"; case 598 -> "rqbnkbrn"; case 599 -> "rqbnkrnb";
            case 600 -> "rbqnbkrn"; case 601 -> "rqnbbkrn"; case 602 -> "rqnkbbrn"; case 603 -> "rqnkbrnb"; case 604 -> "rbqnkrbn";
            case 605 -> "rqnbkrbn"; case 606 -> "rqnkrbbn"; case 607 -> "rqnkrnbb"; case 608 -> "bbrnqkrn"; case 609 -> "brnbqkrn";
            case 610 -> "brnqkbrn"; case 611 -> "brnqkrnb"; case 612 -> "rbbnqkrn"; case 613 -> "rnbbqkrn"; case 614 -> "rnbqkbrn";
            case 615 -> "rnbqkrnb"; case 616 -> "rbnqbkrn"; case 617 -> "rnqbbkrn"; case 618 -> "rnqkbbrn"; case 619 -> "rnqkbrnb";
            case 620 -> "rbnqkrbn"; case 621 -> "rnqbkrbn"; case 622 -> "rnqkrbbn"; case 623 -> "rnqkrnbb"; case 624 -> "bbrnkqrn";
            case 625 -> "brnbkqrn"; case 626 -> "brnkqbrn"; case 627 -> "brnkqrnb"; case 628 -> "rbbnkqrn"; case 629 -> "rnbbkqrn";
            case 630 -> "rnbkqbrn"; case 631 -> "rnbkqrnb"; case 632 -> "rbnkbqrn"; case 633 -> "rnkbbqrn"; case 634 -> "rnkqbbrn";
            case 635 -> "rnkqbrnb"; case 636 -> "rbnkqrbn"; case 637 -> "rnkbqrbn"; case 638 -> "rnkqrbbn"; case 639 -> "rnkqrnbb";
            case 640 -> "bbrnkrqn"; case 641 -> "brnbkrqn"; case 642 -> "brnkrbqn"; case 643 -> "brnkrqnb"; case 644 -> "rbbnkrqn";
            case 645 -> "rnbbkrqn"; case 646 -> "rnbkrbqn"; case 647 -> "rnbkrqnb"; case 648 -> "rbnkbrqn"; case 649 -> "rnkbbrqn";
            case 650 -> "rnkrbbqn"; case 651 -> "rnkrbqnb"; case 652 -> "rbnkrqbn"; case 653 -> "rnkbrqbn"; case 654 -> "rnkrqbbn";
            case 655 -> "rnkrqnbb"; case 656 -> "bbrnkrnq"; case 657 -> "brnbkrnq"; case 658 -> "brnkrbnq"; case 659 -> "brnkrnqb";
            case 660 -> "rbbnkrnq"; case 661 -> "rnbbkrnq"; case 662 -> "rnbkrbnq"; case 663 -> "rnbkrnqb"; case 664 -> "rbnkbrnq";
            case 665 -> "rnkbbrnq"; case 666 -> "rnkrbbnq"; case 667 -> "rnkrbnqb"; case 668 -> "rbnkrnbq"; case 669 -> "rnkbrnbq";
            case 670 -> "rnkrnbbq"; case 671 -> "rnkrnqbb"; case 672 -> "bbqrknnr"; case 673 -> "bqrbknnr"; case 674 -> "bqrknbnr";
            case 675 -> "bqrknnrb"; case 676 -> "qbbrknnr"; case 677 -> "qrbbknnr"; case 678 -> "qrbknbnr"; case 679 -> "qrbknnrb";
            case 680 -> "qbrkbnnr"; case 681 -> "qrkbbnnr"; case 682 -> "qrknbbnr"; case 683 -> "qrknbnrb"; case 684 -> "qbrknnbr";
            case 685 -> "qrkbnnbr"; case 686 -> "qrknnbbr"; case 687 -> "qrknnrbb"; case 688 -> "bbrqknnr"; case 689 -> "brqbknnr";
            case 690 -> "brqknbnr"; case 691 -> "brqknnrb"; case 692 -> "rbbqknnr"; case 693 -> "rqbbknnr"; case 694 -> "rqbknbnr";
            case 695 -> "rqbknnrb"; case 696 -> "rbqkbnnr"; case 697 -> "rqkbbnnr"; case 698 -> "rqknbbnr"; case 699 -> "rqknbnrb";
            case 700 -> "rbqknnbr"; case 701 -> "rqkbnnbr"; case 702 -> "rqknnbbr"; case 703 -> "rqknnrbb"; case 704 -> "bbrkqnnr";
            case 705 -> "brkbqnnr"; case 706 -> "brkqnbnr"; case 707 -> "brkqnnrb"; case 708 -> "rbbkqnnr"; case 709 -> "rkbbqnnr";
            case 710 -> "rkbqnbnr"; case 711 -> "rkbqnnrb"; case 712 -> "rbkqbnnr"; case 713 -> "rkqbbnnr"; case 714 -> "rkqnbbnr";
            case 715 -> "rkqnbnrb"; case 716 -> "rbkqnnbr"; case 717 -> "rkqbnnbr"; case 718 -> "rkqnnbbr"; case 719 -> "rkqnnrbb";
            case 720 -> "bbrknqnr"; case 721 -> "brkbnqnr"; case 722 -> "brknqbnr"; case 723 -> "brknqnrb"; case 724 -> "rbbknqnr";
            case 725 -> "rkbbnqnr"; case 726 -> "rkbnqbnr"; case 727 -> "rkbnqnrb"; case 728 -> "rbknbqnr"; case 729 -> "rknbbqnr";
            case 730 -> "rknqbbnr"; case 731 -> "rknqbnrb"; case 732 -> "rbknqnbr"; case 733 -> "rknbqnbr"; case 734 -> "rknqnbbr";
            case 735 -> "rknqnrbb"; case 736 -> "bbrknnqr"; case 737 -> "brkbnnqr"; case 738 -> "brknnbqr"; case 739 -> "brknnqrb";
            case 740 -> "rbbknnqr"; case 741 -> "rkbbnnqr"; case 742 -> "rkbnnbqr"; case 743 -> "rkbnnqrb"; case 744 -> "rbknbnqr";
            case 745 -> "rknbbnqr"; case 746 -> "rknnbbqr"; case 747 -> "rknnbqrb"; case 748 -> "rbknnqbr"; case 749 -> "rknbnqbr";
            case 750 -> "rknnqbbr"; case 751 -> "rknnqrbb"; case 752 -> "bbrknnrq"; case 753 -> "brkbnnrq"; case 754 -> "brknnbrq";
            case 755 -> "brknnrqb"; case 756 -> "rbbknnrq"; case 757 -> "rkbbnnrq"; case 758 -> "rkbnnbrq"; case 759 -> "rkbnnrqb";
            case 760 -> "rbknbnrq"; case 761 -> "rknbbnrq"; case 762 -> "rknnbbrq"; case 763 -> "rknnbrqb"; case 764 -> "rbknnrbq";
            case 765 -> "rknbnrbq"; case 766 -> "rknnrbbq"; case 767 -> "rknnrqbb"; case 768 -> "bbqrknrn"; case 769 -> "bqrbknrn";
            case 770 -> "bqrknbrn"; case 771 -> "bqrknrnb"; case 772 -> "qbbrknrn"; case 773 -> "qrbbknrn"; case 774 -> "qrbknbrn";
            case 775 -> "qrbknrnb"; case 776 -> "qbrkbnrn"; case 777 -> "qrkbbnrn"; case 778 -> "qrknbbrn"; case 779 -> "qrknbrnb";
            case 780 -> "qbrknrbn"; case 781 -> "qrkbnrbn"; case 782 -> "qrknrbbn"; case 783 -> "qrknrnbb"; case 784 -> "bbrqknrn";
            case 785 -> "brqbknrn"; case 786 -> "brqknbrn"; case 787 -> "brqknrnb"; case 788 -> "rbbqknrn"; case 789 -> "rqbbknrn";
            case 790 -> "rqbknbrn"; case 791 -> "rqbknrnb"; case 792 -> "rbqkbnrn"; case 793 -> "rqkbbnrn"; case 794 -> "rqknbbrn";
            case 795 -> "rqknbrnb"; case 796 -> "rbqknrbn"; case 797 -> "rqkbnrbn"; case 798 -> "rqknrbbn"; case 799 -> "rqknrnbb";
            case 800 -> "bbrkqnrn"; case 801 -> "brkbqnrn"; case 802 -> "brkqnbrn"; case 803 -> "brkqnrnb"; case 804 -> "rbbkqnrn";
            case 805 -> "rkbbqnrn"; case 806 -> "rkbqnbrn"; case 807 -> "rkbqnrnb"; case 808 -> "rbkqbnrn"; case 809 -> "rkqbbnrn";
            case 810 -> "rkqnbbrn"; case 811 -> "rkqnbrnb"; case 812 -> "rbkqnrbn"; case 813 -> "rkqbnrbn"; case 814 -> "rkqnrbbn";
            case 815 -> "rkqnrnbb"; case 816 -> "bbrknqrn"; case 817 -> "brkbnqrn"; case 818 -> "brknqbrn"; case 819 -> "brknqrnb";
            case 820 -> "rbbknqrn"; case 821 -> "rkbbnqrn"; case 822 -> "rkbnqbrn"; case 823 -> "rkbnqrnb"; case 824 -> "rbknbqrn";
            case 825 -> "rknbbqrn"; case 826 -> "rknqbbrn"; case 827 -> "rknqbrnb"; case 828 -> "rbknqrbn"; case 829 -> "rknbqrbn";
            case 830 -> "rknqrbbn"; case 831 -> "rknqrnbb"; case 832 -> "bbrknrqn"; case 833 -> "brkbnrqn"; case 834 -> "brknrbqn";
            case 835 -> "brknrqnb"; case 836 -> "rbbknrqn"; case 837 -> "rkbbnrqn"; case 838 -> "rkbnrbqn"; case 839 -> "rkbnrqnb";
            case 840 -> "rbknbrqn"; case 841 -> "rknbbrqn"; case 842 -> "rknrbbqn"; case 843 -> "rknrbqnb"; case 844 -> "rbknrqbn";
            case 845 -> "rknbrqbn"; case 846 -> "rknrqbbn"; case 847 -> "rknrqnbb"; case 848 -> "bbrknrnq"; case 849 -> "brkbnrnq";
            case 850 -> "brknrbnq"; case 851 -> "brknrnqb"; case 852 -> "rbbknrnq"; case 853 -> "rkbbnrnq"; case 854 -> "rkbnrbnq";
            case 855 -> "rkbnrnqb"; case 856 -> "rbknbrnq"; case 857 -> "rknbbrnq"; case 858 -> "rknrbbnq"; case 859 -> "rknrbnqb";
            case 860 -> "rbknrnbq"; case 861 -> "rknbrnbq"; case 862 -> "rknrnbbq"; case 863 -> "rknrnqbb"; case 864 -> "bbqrkrnn";
            case 865 -> "bqrbkrnn"; case 866 -> "bqrkrbnn"; case 867 -> "bqrkrnnb"; case 868 -> "qbbrkrnn"; case 869 -> "qrbbkrnn";
            case 870 -> "qrbkrbnn"; case 871 -> "qrbkrnnb"; case 872 -> "qbrkbrnn"; case 873 -> "qrkbbrnn"; case 874 -> "qrkrbbnn";
            case 875 -> "qrkrbnnb"; case 876 -> "qbrkrnbn"; case 877 -> "qrkbrnbn"; case 878 -> "qrkrnbbn"; case 879 -> "qrkrnnbb";
            case 880 -> "bbrqkrnn"; case 881 -> "brqbkrnn"; case 882 -> "brqkrbnn"; case 883 -> "brqkrnnb"; case 884 -> "rbbqkrnn";
            case 885 -> "rqbbkrnn"; case 886 -> "rqbkrbnn"; case 887 -> "rqbkrnnb"; case 888 -> "rbqkbrnn"; case 889 -> "rqkbbrnn";
            case 890 -> "rqkrbbnn"; case 891 -> "rqkrbnnb"; case 892 -> "rbqkrnbn"; case 893 -> "rqkbrnbn"; case 894 -> "rqkrnbbn";
            case 895 -> "rqkrnnbb"; case 896 -> "bbrkqrnn"; case 897 -> "brkbqrnn"; case 898 -> "brkqrbnn"; case 899 -> "brkqrnnb";
            case 900 -> "rbbkqrnn"; case 901 -> "rkbbqrnn"; case 902 -> "rkbqrbnn"; case 903 -> "rkbqrnnb"; case 904 -> "rbkqbrnn";
            case 905 -> "rkqbbrnn"; case 906 -> "rkqrbbnn"; case 907 -> "rkqrbnnb"; case 908 -> "rbkqrnbn"; case 909 -> "rkqbrnbn";
            case 910 -> "rkqrnbbn"; case 911 -> "rkqrnnbb"; case 912 -> "bbrkrqnn"; case 913 -> "brkbrqnn"; case 914 -> "brkrqbnn";
            case 915 -> "brkrqnnb"; case 916 -> "rbbkrqnn"; case 917 -> "rkbbrqnn"; case 918 -> "rkbrqbnn"; case 919 -> "rkbrqnnb";
            case 920 -> "rbkrbqnn"; case 921 -> "rkrbbqnn"; case 922 -> "rkrqbbnn"; case 923 -> "rkrqbnnb"; case 924 -> "rbkrqnbn";
            case 925 -> "rkrbqnbn"; case 926 -> "rkrqnbbn"; case 927 -> "rkrqnnbb"; case 928 -> "bbrkrnqn"; case 929 -> "brkbrnqn";
            case 930 -> "brkrnbqn"; case 931 -> "brkrnqnb"; case 932 -> "rbbkrnqn"; case 933 -> "rkbbrnqn"; case 934 -> "rkbrnbqn";
            case 935 -> "rkbrnqnb"; case 936 -> "rbkrbnqn"; case 937 -> "rkrbbnqn"; case 938 -> "rkrnbbqn"; case 939 -> "rkrnbqnb";
            case 940 -> "rbkrnqbn"; case 941 -> "rkrbnqbn"; case 942 -> "rkrnqbbn"; case 943 -> "rkrnqnbb"; case 944 -> "bbrkrnnq";
            case 945 -> "brkbrnnq"; case 946 -> "brkrnbnq"; case 947 -> "brkrnnqb"; case 948 -> "rbbkrnnq"; case 949 -> "rkbbrnnq";
            case 950 -> "rkbrnbnq"; case 951 -> "rkbrnnqb"; case 952 -> "rbkrbnnq"; case 953 -> "rkrbbnnq"; case 954 -> "rkrnbbnq";
            case 955 -> "rkrnbnqb"; case 956 -> "rbkrnnbq"; case 957 -> "rkrbnnbq"; case 958 -> "rkrnnbbq"; case 959 -> "rkrnnqbb";
            default  -> "rnbqkbnr";
        };
        return "%s w %s - 0 1".formatted(positionsByMirroredPieces(pieces), initialCastlingByPiecesSetup(pieces));
    }
}
