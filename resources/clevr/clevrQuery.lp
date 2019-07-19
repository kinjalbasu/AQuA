:- include('clevrKnowledge.pl').
:- include('clevrRules.pl').
:- include('clevrSemanticRules.pl').
:- include('clevrCommonFacts.pl').
find_ans(Q) :- question(Q),find_all_filters(thing, 7, L),list_object(L, Ids),list_length(Ids, C),quantification(N, thing_7),gte(C, N).
question('is there a big blue rubber thing ?').
question_answer('is there a big blue rubber thing ?', true) :- find_ans('is there a big blue rubber thing ?').
question_answer('is there a big blue rubber thing ?', false) :- not find_ans('is there a big blue rubber thing ?').
query(Q, A) :- question(Q),question_answer(Q, A).
