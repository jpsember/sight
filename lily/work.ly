\version "2.24.3"

% Render the following components in parallel
%

<<

\new Staff {

    \clef treble
    \omit Score.BarLine
    \omit Staff.TimeSignature

    % Add extra space between the key signature and the first note
    \override Staff.KeySignature.space-alist.first-note = #'(extra-space . 8)
    % In case there is no key signature, add space from clef
    \override Staff.Clef.space-alist.first-note = #'(extra-space . 8)

    \key des \major
     <c' f' bes'>2
}


\new Staff {

    \clef bass
    \omit Score.BarLine
    \omit Staff.TimeSignature

    % Add extra space between the key signature and the first note
    \override Staff.KeySignature.space-alist.first-note = #'(extra-space . 8)
    % In case there is no key signature, add space from clef
    \override Staff.Clef.space-alist.first-note = #'(extra-space . 8)

   \key des \major
     <des ges bes>2
     <c f a>2
     <cis fis ais>2
}


>>


% Increase the separation between adjacent notes (chords)
%
\layout {
  \context {
    \Score
    \override SpacingSpanner.base-shortest-duration = #(ly:make-moment 1/64)
  }
}


% Suppress the footer
%
\header {
  tagline = ""  % removed
}
