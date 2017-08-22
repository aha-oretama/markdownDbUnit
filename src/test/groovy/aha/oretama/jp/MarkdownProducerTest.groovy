package aha.oretama.jp

import spock.lang.Specification

/**
 * @author aha-oretama
 */
class MarkdownProducerTest extends Specification {

  def produceFromMarkdownTest() {

    expect:
    new MarkdownProducer(new File("src/test/resources/testdata.md")).produceFromMarkdown()

  }
}
