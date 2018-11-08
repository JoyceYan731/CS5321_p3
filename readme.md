UPDATE: We have figured out the external sort operator which we did not in project 2. Therefore this project supports all
kinds of configurations in the form of the project guidance of 2 and 3.

Part 1: Top level entry of the project.
    The main function is located at src/App/Main.java
    The SQLInterpreter will reads the input, build the query plan and thus write the output.

Part 2: Implementing Outline of IndexScanOperator

    # Set the lowkey and highkey (In our code they are lowerBound and upperBound)
	 When the program constructs the physical query plan from logical query plan, it will check if the scan conditions of 
	 the logical scan operator are suitable for index tree-based way of searching tuples. If (part of) these conditions are, 
	 we can construct the IndexScanOperator by given table name, table alias, the index column as well as the 
	 lower bound and upper bound. Hence the indexScanOperator gets its upper bound and lower bound from the physical plan
	 builder, which has used visit pattern to discriminate the portion of index scan operator expressions from other expressions, and 
	 set the lowkey and highkey, which will be explained in Part 3.
 
	 For example, if the scan conditions for a scan logical operator with tableName to be "Sailors" is
	 "S.A > 12 AND S.B = 3", and there is an index for table sailors indexed by the column of A, we are
	 able to build the constructor by 
 
       new IndexScanOperator("Sailors", "S", "A", 13, null);
      
	This IndexScanOperator has the public method "getNextTuple()". Every time it is called,
	this operator will return the next row of Sailors which are qualified for "S.A > 12". 
	
	# About cluster and uncluster
	The way this operator find qualified tuples are determined by whether the index is clustered or not. The code starts in line 242 of file "src/operators/IndexScanOperator.java". From the infomation gained  in "src/Data/DataBase" class, the program is able to see if the relation
    index used by this operator is clustered. If it is, then it only traverses for one time across  the index file, find the first qualified list of tuples from
     the leaf page, and get Tuple right from the sorted relation data, and never look back for the index page or the leaf page again; else if the table 
     index is unclustered, after the file channal has been positioned at the targeted <key, rid> pair on the leaf page, it will continue scanning the leaf page every time getNextTuple() is called
	
	 #Deserialization from root to leaf
	 The way that IndexScanOperator finds tuples is to look up the index tree of Sailors in "input/db/indexes" directory and find the tuples guided
	 by the addresses and rids in it. Firstly we get the address of the leaf page whose key range  contains the target key, by searching across the index pages by recursion. The base case is when a leaf page is found and thus  return the address (the page number in the index file); the recursion rule is whenever a index page is reached, scan all keys to find the position of the target key, and return the address of the corresponding child index page (or leaf page). 
	 
	 Only the nodes on the one straight way from root to leaf in the index tree are deserialized.
	 
Part 3: Physical Plan Builder to Seperate the IndexScanOperator Consitions from the normal Scan Conditions

	We build a class “IndexExpressionVisitor” to classify the expressions. If the expression includes two columns, it cannot be handled by index and this condition will be stored in a List “unindexedConditions”. If the express includes only one column and it is in the index information, then this expression can be handled by index and the upper bound and lower bound will be updated. Otherwise, this expression will also be stored in “unindexedConditions”. After visiting all the expressions, the expressions in “unindexedConditions” will be combined in one expression.
	In the “PhysicalPlanVisitor, once the “LogicalScanOperator” is visited, we need to initialize an “IndexedExpressionVisitor” to visit the expression of the “LogicalScanOperator” first. If both the upper bound and the lower bound are “null”, then only a full-scan operator will be created. Othrwise, an “IndexScanOperator” will be created first with the information of upper bound and lower bound. And a full-scan operator will be built with the only one expression in the “unindexedCondition”. Besides that, the previously built “IndexedExpressionVisitor” will be the child operator of this full-scan operator.
	
Part 4: Serialization of relation data (generating the index file)

	In term of index tree, we build different index trees for every required relation once we initialize an index tree builder.
	For the goal of OOD, we add Node class for this index tree so that we can get keys easily.
	
	Because the data is static it is in our interest to fill the tree completely (to keep it as short as possible). Thus every leaf node gets 2d data entries. However, this may leave us with < d data entries for the last leaf node. If this case happens, we handle the second-to-last leaf node and the last leaf node specially, as follows. Assume we have two nodes left to construct and have k data entries, with 2d < k < 3d. Then the second-to-last node gets k=2 entries, and the last node gets the remainder.
	
	Similarly, we then build the layer of index nodes that sits directly above the leaf layer. Every index node gets 2d keys and 2d + 1 children, except possibly the last two nodes to avoid an underfull situation as before. If we have two index nodes left to construct, and have a total of m children, with 2d + 1 < m < 3d + 2, give the second-to-last node m=2 children and the remainder of the children to the last node.
	
	When choosing an integer to serve as a key inside an index node, consider the subtree corresponding to the pointer after the key. Use the smallest search key found in the leftmost leaf of this subtree.
	
	In detail, the index tree builder will firstly generate all leaf Nodes in the same level, and then does level by level traverse to generate index nodes until we reach the root node. If the tree is so small that it only has one leaf node, the index tree will finally present as a two level tree which root node has no key but only a pointer pointing to index node.