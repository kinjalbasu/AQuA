question('how many fruits are there ?', 1).
question('how many fruits are there ?', certain, X1) :- _mod(fruit, many),_property(E1, be, _by, S1),_similar(fruit, S1),_similar(many_fruit, S1),event(E1, be, _, O1),total_element(fruit, X1).
question('how many fruits are there ?', certain, X1) :- _mod(fruit, many),_relation(S1, E1, _clause),_similar(fruit, S1),_similar(many_fruit, S1),event(E1, be, _, _),total_element(fruit, X1).
question('how many fruits are there ?', certain, X1) :- _mod(fruit, many),_similar(fruit, S1),_similar(many_fruit, S1),event(E1, be, S1, O1),total_element(fruit, X1).
question('how many fruits are there ?', likely, X1) :- _property(E1, be, _by, S1),_similar(fruit, S1),_similar(many_fruit, S1),event(E1, be, _, O1),total_element(fruit, X1).
question('how many fruits are there ?', likely, X1) :- _relation(S1, E1, _clause),_similar(fruit, S1),_similar(many_fruit, S1),event(E1, be, _, _),total_element(fruit, X1).
question('how many fruits are there ?', likely, X1) :- _similar(fruit, S1),_similar(many_fruit, S1),event(E1, be, S1, O1),total_element(fruit, X1).
question('how many fruits are there ?', possible, X1) :- total_element(fruit, X1).
?- query(1,Question,answer( Answer, Confidence_Level)).