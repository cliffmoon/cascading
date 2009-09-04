/*
 * Copyright (c) 2007-2009 Concurrent, Inc. All Rights Reserved.
 *
 * Project and contact information: http://www.cascading.org/
 *
 * This file is part of the Cascading project.
 *
 * Cascading is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Cascading is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Cascading.  If not, see <http://www.gnu.org/licenses/>.
 */

package cascading.flow;

import java.io.IOException;

import cascading.CascadingTestCase;
import cascading.cascade.Cascade;
import cascading.cascade.CascadeConnector;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;

/**
 *
 */
public class MapReduceFlowTest extends CascadingTestCase
  {
  String inputFileApache = "build/test/data/apache.10.txt";
  String outputPath = "build/test/output/mapreduceflow/";
  String outputPath1 = "build/test/output/mapreducecascade1/";
  String outputPath2 = "build/test/output/mapreducecascade2/";
  String outputPath3 = "build/test/output/mapreducecascade3/";

  public MapReduceFlowTest()
    {
    super( "map-reduce flow test" );
    }

  public void testFlow() throws IOException
    {
    Job conf = new Job();
    conf.setJobName( "mrflow" );

    conf.setOutputKeyClass( LongWritable.class );
    conf.setOutputValueClass( Text.class );

//    conf.setMapperClass( IdentityMapper.class );
//    conf.setReducerClass( IdentityReducer.class );

    conf.setInputFormatClass( TextInputFormat.class );
    conf.setOutputFormatClass( TextOutputFormat.class );

    FileInputFormat.setInputPaths( conf, new Path( inputFileApache ) );
    FileOutputFormat.setOutputPath( conf, new Path( outputPath ) );

    Flow flow = new MapReduceFlow( "mrflow", conf, true );

    validateLength( flow.openSource(), 10 );

    flow.complete();

    validateLength( flow.openSink(), 10 );
    }

  public void testCascade() throws IOException
    {
    Job firstConf = new Job();
    firstConf.setJobName( "first" );

    firstConf.setOutputKeyClass( LongWritable.class );
    firstConf.setOutputValueClass( Text.class );

//    firstConf.setMapperClass( IdentityMapper.class );
//    firstConf.setReducerClass( IdentityReducer.class );

    firstConf.setInputFormatClass( TextInputFormat.class );
    firstConf.setOutputFormatClass( TextOutputFormat.class );

    FileInputFormat.setInputPaths( firstConf, new Path( inputFileApache ) );
    FileOutputFormat.setOutputPath( firstConf, new Path( outputPath1 ) );

    Flow firstFlow = new MapReduceFlow( firstConf, true );

    Job secondConf = new Job();
    secondConf.setJobName( "second" );

    secondConf.setOutputKeyClass( LongWritable.class );
    secondConf.setOutputValueClass( Text.class );

//    secondConf.setMapperClass( IdentityMapper.class );
//    secondConf.setReducerClass( IdentityReducer.class );

    secondConf.setInputFormatClass( TextInputFormat.class );
    secondConf.setOutputFormatClass( TextOutputFormat.class );

    FileInputFormat.setInputPaths( secondConf, new Path( outputPath1 ) );
    FileOutputFormat.setOutputPath( secondConf, new Path( outputPath2 ) );

    Flow secondFlow = new MapReduceFlow( secondConf, true );

    Job thirdConf = new Job();
    thirdConf.setJobName( "third" );

    thirdConf.setOutputKeyClass( LongWritable.class );
    thirdConf.setOutputValueClass( Text.class );

//    thirdConf.setMapperClass( IdentityMapper.class );
//    thirdConf.setReducerClass( IdentityReducer.class );

    thirdConf.setInputFormatClass( TextInputFormat.class );
    thirdConf.setOutputFormatClass( TextOutputFormat.class );

    FileInputFormat.setInputPaths( thirdConf, new Path( outputPath2 ) );
    FileOutputFormat.setOutputPath( thirdConf, new Path( outputPath3 ) );

    Flow thirdFlow = new MapReduceFlow( thirdConf, true );

    CascadeConnector cascadeConnector = new CascadeConnector();

    Cascade cascade = cascadeConnector.connect( firstFlow, secondFlow, thirdFlow );

    cascade.complete();

    validateLength( thirdFlow.openSink(), 10 );
    }
  }
