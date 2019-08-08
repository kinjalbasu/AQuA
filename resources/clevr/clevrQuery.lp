:-style_check(-singleton).
:-style_check(-discontiguous).
:- include('clevrKnowledge.pl').
:- include('clevrRules.pl').
:- include('clevrSemanticRules.pl').
:- include('clevrCommonFacts.pl').
question('are there fewer tiny green metallic spheres behind the yellow rubber thing than yellow cylinders ?').
question_answer('are there fewer tiny green metallic spheres behind the yellow rubber thing than yellow cylinders ?', yes) :- find_ans('are there fewer tiny green metallic spheres behind the yellow rubber thing than yellow cylinders ?').
question_answer('are there fewer tiny green metallic spheres behind the yellow rubber thing than yellow cylinders ?', no) :- not find_ans('are there fewer tiny green metallic spheres behind the yellow rubber thing than yellow cylinders ?').
query(Q, A) :- question(Q),question_answer(Q, A).
