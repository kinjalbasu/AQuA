:- include('clevrKnowledge.pl').
:- include('clevrRules.pl').
:- include('clevrSemanticRules.pl').
:- include('clevrCommonFacts.pl').
question('the sphere is what size ?').
question_answer('the sphere is what size ?', A) :- find_ans('the sphere is what size ?', A).
query(Q, A) :- question(Q),question_answer(Q, A).
