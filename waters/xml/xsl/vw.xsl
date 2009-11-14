<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns="http://waters.sourceforge.net/xsd/module">

<xsl:output method="xml" encoding="UTF-8" indent="yes"/>


<!-- ==================================================================== -->
<!-- Module                                                               -->
<!-- ==================================================================== -->

<xsl:template match="module">
  <Module>
    <xsl:variable name="suffix">
      <xsl:call-template name="get-suffix">
        <xsl:with-param name="suffixedname" select="@name"/>
      </xsl:call-template>
    </xsl:variable>
    <xsl:attribute name="Name">
      <xsl:value-of
        select="substring(@name,1,
                          string-length(@name)-string-length($suffix)-1)"/>
    </xsl:attribute>
    <xsl:apply-templates select="*"/>
  </Module>
</xsl:template>


<!-- ==================================================================== -->
<!-- Module / ConstantAliasList                                           -->
<!-- ==================================================================== -->

<xsl:template match="definitions[count(./*) = 0]"/>

<xsl:template match="definitions">
  <ConstantAliasList>
    <xsl:apply-templates select="*"/>
  </ConstantAliasList>
</xsl:template>

<xsl:template match="typeDefinition">
  <ConstantAlias>
    <SimpleIdentifier>
      <xsl:attribute name="Name">
        <xsl:value-of select="@name"/>
      </xsl:attribute>
    </SimpleIdentifier>
    <ConstantAliasExpression>
      <xsl:call-template name="parse-range">
        <xsl:with-param name="expr" select="@expression"/>
      </xsl:call-template>
    </ConstantAliasExpression>
  </ConstantAlias>
</xsl:template>


<!-- ==================================================================== -->
<!-- Module / EventDeclList                                               -->
<!-- ==================================================================== -->

<xsl:template match="interface[count(./*) = 0]"/>

<xsl:template match="local[count(./*) = 0]"/>

<xsl:template match="local">
  <EventDeclList>
    <xsl:apply-templates select="event"/>
    <EventDecl Name=":accepting" Kind="PROPOSITION"/>
  </EventDeclList>
</xsl:template>

<xsl:template match="local/event">
  <EventDecl>
    <xsl:attribute name="Name">
      <xsl:value-of select="@name"/>
    </xsl:attribute>
    <xsl:attribute name="Kind">
      <xsl:if test="@controllable = 0">
        <xsl:text>UNCONTROLLABLE</xsl:text>
      </xsl:if>
      <xsl:if test="@controllable = 1">
        <xsl:text>CONTROLLABLE</xsl:text>
      </xsl:if>
    </xsl:attribute>
    <xsl:if test="local-name(following::*[1]) = 'foreach-alias'">
      <RangeList>
        <xsl:apply-templates select="following::*[1]"/>
      </RangeList>
    </xsl:if>
    <xsl:apply-templates select="comment"/>
  </EventDecl>
</xsl:template>

<xsl:template match="foreach-alias">
  <xsl:call-template name="parse-range">
    <xsl:with-param name="expr" select="@range"/>
  </xsl:call-template>
  <xsl:apply-templates select="foreach-alias"/>
</xsl:template>

<xsl:template match="comment">
  <Comment>
    <xsl:apply-templates select="node()"/>
  </Comment>
</xsl:template>


<!-- ==================================================================== -->
<!-- Module / ComponentList                                               -->
<!-- ==================================================================== -->

<xsl:template match="parts[count(./*) = 0]"/>

<xsl:template match="parts">
  <ComponentList>
    <xsl:apply-templates select="*">
      <xsl:sort select="concat(@graph,descendant-or-self::instance/@module)"/>
    </xsl:apply-templates>
  </ComponentList>
</xsl:template>

<xsl:template match="component[@kind = 'process']"/>

<xsl:template match="component">
  <SimpleComponent>
    <xsl:call-template name="set-kind">
      <xsl:with-param name="kind" select="@kind"/>
    </xsl:call-template>
    <SimpleIdentifier>
      <xsl:attribute name="Name">
        <xsl:value-of select="@graph"/>
      </xsl:attribute>
    </SimpleIdentifier>
    <xsl:apply-templates select="document(concat(@graph,'.dgrf'),.)"/>
  </SimpleComponent>
</xsl:template>

<xsl:template match="foreach-instance">
  <ForeachComponent>
    <xsl:attribute name="Name">
      <xsl:value-of select="@dummy"/>
    </xsl:attribute>
    <xsl:call-template name="parse-range">
      <xsl:with-param name="expr" select="@range"/>
    </xsl:call-template>
    <ComponentList>
      <xsl:apply-templates select="*"/>
    </ComponentList>
  </ForeachComponent>
</xsl:template>

<xsl:template match="instance">
  <SimpleComponent>
    <xsl:variable name="kind">
      <xsl:call-template name="get-suffix">
        <xsl:with-param name="suffixedname" select="@module"/>
      </xsl:call-template>
    </xsl:variable>
    <xsl:variable name="graph"
      select="substring(@module,1,
                        string-length(@module)-string-length($kind)-1)"/>
    <xsl:call-template name="set-kind">
      <xsl:with-param name="kind" select="$kind"/>
    </xsl:call-template>
    <xsl:call-template name="parse-identifier3">
      <xsl:with-param name="expr" select="@name"/>
    </xsl:call-template>      
    <xsl:apply-templates select="document(concat($graph,'.dgrf'),.)"/>
  </SimpleComponent>
</xsl:template>


<!-- ==================================================================== -->
<!-- Graph                                                                -->
<!-- ==================================================================== -->

<xsl:template match="graph">
  <Graph>
    <xsl:if test="@generatorName ='VALID-II'">
      <xsl:attribute name="Deterministic">
        <xsl:text>false</xsl:text>
      </xsl:attribute>
    </xsl:if>
    <xsl:apply-templates select="*"/>
  </Graph>
</xsl:template>

<xsl:template
  match="events[count(event[not(@name=../../edges//label/@name)]) = 0]"/>

<xsl:template match="events">
  <LabelBlock>
    <xsl:apply-templates select="*"/>
  </LabelBlock>
</xsl:template>

<xsl:template match="events/event">
  <xsl:if test="not(@name=../../edges//label/@name)">
    <xsl:call-template name="parse-identifier2">
      <xsl:with-param name="expr" select="@name"/>
    </xsl:call-template>
  </xsl:if>
</xsl:template>

<xsl:template match="nodes[count(./*) = 0]"/>

<xsl:template match="nodes">
  <NodeList>
    <xsl:apply-templates select="*"/>
  </NodeList>
</xsl:template>

<xsl:template match="node">
  <SimpleNode>
    <xsl:attribute name="Name">
      <xsl:value-of select="label/@name"/>
    </xsl:attribute>
    <xsl:if test="@initial = 1">
      <xsl:attribute name="Initial">
        <xsl:text>true</xsl:text>
      </xsl:attribute>
    </xsl:if>
    <xsl:if test="@marked = 1">
      <EventList>
        <SimpleIdentifier Name=":accepting"/>
      </EventList>
    </xsl:if>
    <xsl:apply-templates select=".//rendering"/>
  </SimpleNode>
</xsl:template>

<xsl:template match="nodeGroup">
  <GroupNode>
    <xsl:attribute name="Name">
      <xsl:call-template name="replace-dollars">
        <xsl:with-param name="name" select="@id"/>
      </xsl:call-template>
    </xsl:attribute>
    <xsl:apply-templates select="nodeElement"/>
    <xsl:apply-templates select="rendering"/>
  </GroupNode>
</xsl:template>

<xsl:template match="nodeElement">
  <NodeRef>
    <xsl:attribute name="Name">
      <xsl:call-template name="replace-dollars">
        <xsl:with-param name="name" select="@name"/>
      </xsl:call-template>
    </xsl:attribute>
  </NodeRef>
</xsl:template>

<xsl:template match="edges[count(./*) = 0]"/>

<xsl:template match="edges">
  <EdgeList>
    <xsl:apply-templates select="*"/>
  </EdgeList>
</xsl:template>

<xsl:template match="edge">
  <Edge>
    <xsl:attribute name="Source">
      <xsl:call-template name="replace-dollars">
        <xsl:with-param name="name" select="source/@name"/>
      </xsl:call-template>
    </xsl:attribute>
    <xsl:choose>
      <xsl:when test="@isLoop = 1">
        <xsl:attribute name="Target">
          <xsl:value-of select="source/@name"/>
        </xsl:attribute>
      </xsl:when>
      <xsl:otherwise>
        <xsl:attribute name="Target">
          <xsl:value-of select="target/@name"/>
        </xsl:attribute>
      </xsl:otherwise>
    </xsl:choose>
    <xsl:apply-templates select="labelGroup"/>
    <xsl:apply-templates select="rendering"/>
  </Edge>
</xsl:template>

<xsl:template match="labelGroup">
  <LabelBlock>
    <xsl:apply-templates select="label"/>
    <xsl:apply-templates select="rendering"/>
  </LabelBlock>
</xsl:template>

<xsl:template match="label">
  <xsl:call-template name="parse-identifier2">
    <xsl:with-param name="expr" select="@name"/>
  </xsl:call-template>
</xsl:template>


<!-- ==================================================================== -->
<!-- Geometry                                                             -->
<!-- ==================================================================== -->

<xsl:template match="node/rendering">
  <PointGeometry>
    <xsl:apply-templates select="*"/>
  </PointGeometry>
</xsl:template>

<xsl:template match="nodeGroup/rendering">
  <BoxGeometry>
    <xsl:apply-templates select="boundary/rectangle"/>
  </BoxGeometry>
</xsl:template>

<xsl:template match="label/rendering">
  <LabelGeometry>
    <xsl:apply-templates select="*"/>
  </LabelGeometry>
</xsl:template>

<xsl:template match="labelGroup/rendering">
  <LabelGeometry>
    <xsl:attribute name="Anchor">
      <xsl:text>NW</xsl:text>
    </xsl:attribute>
    <Point>
      <xsl:attribute name="X">
        <xsl:value-of select="point/@x"/>
      </xsl:attribute>
      <xsl:attribute name="Y">
        <xsl:value-of select="point/@y - 10"/>
      </xsl:attribute>
    </Point>
  </LabelGeometry>
</xsl:template>

<xsl:template match="edge/rendering">
  <SplineGeometry>
    <xsl:apply-templates select="*"/>
  </SplineGeometry>
</xsl:template>

<xsl:template match="point">
  <Point>
    <xsl:attribute name="X">
      <xsl:value-of select="@x"/>
    </xsl:attribute>
    <xsl:attribute name="Y">
      <xsl:value-of select="@y"/>
    </xsl:attribute>
  </Point>
</xsl:template>

<xsl:template match="rectangle">
  <Box>
    <xsl:attribute name="X">
      <xsl:value-of select="@x"/>
    </xsl:attribute>
    <xsl:attribute name="Y">
      <xsl:value-of select="@y"/>
    </xsl:attribute>
    <xsl:attribute name="Width">
      <xsl:value-of select="@width"/>
    </xsl:attribute>
    <xsl:attribute name="Height">
      <xsl:value-of select="@height"/>
    </xsl:attribute>
  </Box>
</xsl:template>


<!-- ==================================================================== -->
<!-- VALID Expression Parsing                                             -->
<!-- ==================================================================== -->

<xsl:template name="parse-range">
  <xsl:param name="expr"/>
  <xsl:choose>
    <xsl:when test="contains($expr,'..')">
      <xsl:variable
        name="body"
        select="substring($expr,2,string-length($expr)-2)"/>
      <BinaryExpression>
        <xsl:attribute name="Operator">
          <xsl:text>..</xsl:text>
        </xsl:attribute>
        <IntConstant>
          <xsl:attribute name="Value">
            <xsl:value-of select="substring-before($body,'..')"/>
          </xsl:attribute>
        </IntConstant>
        <IntConstant>
          <xsl:attribute name="Value">
            <xsl:value-of select="substring-after($body,'..')"/>
          </xsl:attribute>
        </IntConstant>
      </BinaryExpression>
    </xsl:when>
    <xsl:when test="starts-with($expr,'{')">
      <xsl:variable
        name="body"
        select="substring($expr,2,string-length($expr)-2)"/>
      <xsl:choose>
        <xsl:when test="format-number($body,'#')='NaN'">
          <EnumSetExpression>
            <xsl:call-template name="collect-enum-members">
              <xsl:with-param name="list" select="normalize-space($body)"/>
            </xsl:call-template>
          </EnumSetExpression>
        </xsl:when>
        <xsl:otherwise>
          <BinaryExpression>
            <xsl:attribute name="Operator">
              <xsl:text>..</xsl:text>
            </xsl:attribute>
            <IntConstant>
              <xsl:attribute name="Value">
                <xsl:value-of select="$body"/>
              </xsl:attribute>
            </IntConstant>
            <IntConstant>
              <xsl:attribute name="Value">
                <xsl:value-of select="$body"/>
              </xsl:attribute>
            </IntConstant>
          </BinaryExpression>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:when>
    <xsl:when test="starts-with($expr,'$')">
      <SimpleIdentifier>
        <xsl:attribute name="Name">
          <xsl:value-of select="substring-after($expr,'$')"/>
        </xsl:attribute>
      </SimpleIdentifier>
    </xsl:when>
  </xsl:choose>
</xsl:template>

<xsl:template name="parse-identifier2">
  <xsl:param name="expr"/>
  <xsl:choose>
    <xsl:when test="contains($expr,'.')">
      <IndexedIdentifier>
        <xsl:attribute name="Name">
          <xsl:value-of select="substring-before($expr,'.')"/>
        </xsl:attribute>
        <xsl:call-template name="collect-indexes2">
          <xsl:with-param name="list" select="substring-after($expr,'.')"/>
        </xsl:call-template>
      </IndexedIdentifier>
    </xsl:when>
    <xsl:otherwise>
      <SimpleIdentifier>
        <xsl:attribute name="Name">
          <xsl:value-of select="$expr"/>
        </xsl:attribute>
      </SimpleIdentifier>      
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<xsl:template name="parse-identifier3">
  <xsl:param name="expr"/>
  <xsl:choose>
    <xsl:when test="contains($expr,'[')">
      <IndexedIdentifier>
        <xsl:variable name="prefix">
          <xsl:value-of select="substring-before($expr,'[')"/>
        </xsl:variable>
        <xsl:attribute name="Name">
          <xsl:value-of select="$prefix"/>
        </xsl:attribute>
        <xsl:call-template name="collect-indexes3">
          <xsl:with-param name="list"
            select="substring($expr,1+string-length($prefix))"/>
        </xsl:call-template>
      </IndexedIdentifier>
    </xsl:when>
    <xsl:otherwise>
      <SimpleIdentifier>
        <xsl:attribute name="Name">
          <xsl:value-of select="$expr"/>
        </xsl:attribute>
      </SimpleIdentifier>      
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<xsl:template name="parse-atom">
  <xsl:param name="expr"/>
  <xsl:choose>
    <xsl:when test="starts-with($expr,'$')">
      <SimpleIdentifier>
        <xsl:attribute name="Name">
          <xsl:value-of select="substring-after($expr,'$')"/>
        </xsl:attribute>
      </SimpleIdentifier>
    </xsl:when>
    <xsl:when test="format-number($expr,'#')='NaN'">
      <SimpleIdentifier>
        <xsl:attribute name="Name">
          <xsl:value-of select="$expr"/>
        </xsl:attribute>
      </SimpleIdentifier>      
    </xsl:when>
    <xsl:otherwise>
      <IntConstant>
        <xsl:attribute name="Value">
          <xsl:value-of select="$expr"/>
        </xsl:attribute>
      </IntConstant>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<xsl:template name="parse-enum-atom">
  <xsl:param name="expr"/>
  <xsl:choose>
    <xsl:when test="format-number($expr,'#')='NaN'">
      <SimpleIdentifier>
        <xsl:attribute name="Name">
          <xsl:value-of select="$expr"/>
        </xsl:attribute>
      </SimpleIdentifier>      
    </xsl:when>
    <xsl:otherwise>
      <SimpleIdentifier>
        <xsl:attribute name="Name">
          <xsl:text>:</xsl:text>
          <xsl:value-of select="$expr"/>
        </xsl:attribute>
      </SimpleIdentifier>      
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<xsl:template name="collect-enum-members">
  <xsl:param name="list"/>
  <xsl:choose>
    <xsl:when test="string-length($list)=0"/>
    <xsl:when test="contains($list,' ')">
      <xsl:call-template name="parse-enum-atom">
        <xsl:with-param name="expr" select="substring-before($list,' ')"/>
      </xsl:call-template>
      <xsl:call-template name="collect-enum-members">
        <xsl:with-param
          name="list"
          select="normalize-space(substring-after($list,' '))"/>
      </xsl:call-template>      
    </xsl:when>
    <xsl:otherwise>
      <xsl:call-template name="parse-enum-atom">
        <xsl:with-param name="expr" select="$list"/>
      </xsl:call-template>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<xsl:template name="collect-indexes2">
  <xsl:param name="list"/>
  <xsl:choose>
    <xsl:when test="contains($list,'.')">
      <xsl:call-template name="parse-atom">
        <xsl:with-param name="expr" select="substring-before($list,'.')"/>
      </xsl:call-template>
      <xsl:call-template name="collect-indexes2">
        <xsl:with-param name="list" select="substring-after($list,'.')"/>
      </xsl:call-template>      
    </xsl:when>
    <xsl:otherwise>
      <xsl:call-template name="parse-atom">
        <xsl:with-param name="expr" select="$list"/>
      </xsl:call-template>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<xsl:template name="collect-indexes3">
  <xsl:param name="list"/>
  <xsl:if test="starts-with($list,'[')">
    <xsl:call-template name="parse-atom">
      <xsl:with-param name="expr"
        select="substring-before(substring($list,2),']')"/>
    </xsl:call-template>
    <xsl:call-template name="collect-indexes3">
      <xsl:with-param name="list" select="substring-after($list,']')"/>
    </xsl:call-template>      
  </xsl:if>
</xsl:template>

<xsl:template name="get-suffix">
  <xsl:param name="suffixedname"/>
  <xsl:choose>
    <xsl:when test="contains($suffixedname,'_')">
      <xsl:call-template name="get-suffix">
        <xsl:with-param
          name="suffixedname"
          select="substring-after($suffixedname,'_')"/>
      </xsl:call-template>
    </xsl:when>
    <xsl:otherwise>
      <xsl:value-of select="$suffixedname"/>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<xsl:template name="set-kind">
  <xsl:param name="kind"/>
  <xsl:attribute name="Kind">
    <xsl:choose>
      <xsl:when test="$kind = 'plant'">
        <xsl:text>PLANT</xsl:text>
      </xsl:when>
      <xsl:when test="$kind = 'spec'">
        <xsl:text>SPEC</xsl:text>
      </xsl:when>
      <xsl:when test="$kind = 'property'">
        <xsl:text>PROPERTY</xsl:text>
      </xsl:when>
    </xsl:choose>
  </xsl:attribute>
</xsl:template>

<xsl:template name="replace-dollars">
  <xsl:param name="name"/>
  <xsl:choose>
    <xsl:when test="starts-with($name,'$')">
      <xsl:text>:</xsl:text>
      <xsl:value-of select="substring($name,2)"/>
    </xsl:when>
    <xsl:otherwise>
      <xsl:value-of select="$name"/>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>


<!-- ==================================================================== -->
<!-- Copy anything else                                                   -->
<!-- ==================================================================== -->

<xsl:template match="/ | @* | node()">
  <xsl:copy>
    <xsl:apply-templates select="@* | node()"/>
  </xsl:copy>
</xsl:template>

</xsl:stylesheet>

